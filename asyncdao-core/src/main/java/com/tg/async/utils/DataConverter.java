package com.tg.async.utils;

import com.tg.async.dynamic.mapping.ColumnMapping;
import com.tg.async.dynamic.mapping.ModelMap;
import com.tg.async.exception.ParseException;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by twogoods on 2018/5/2.
 */
public class DataConverter {
    private static final Logger log = LoggerFactory.getLogger(DataConverter.class);
    private static Map<Class, Map<String, PropertyDescriptor>> classesWithProperty = new ConcurrentHashMap<>();

    public static <T> List<T> queryResultToListObject(RowSet<Row> queryResult, Class<T> clazz, ModelMap resultMap) {
        List<T> list = new ArrayList<T>();
        List<String> columnNames = queryResult.columnsNames();
        if (columnNames == null) {
            return list;
        }
        for (Row row : queryResult) {
            try {
                list.add(rowDataToObject(row, clazz, resultMap, columnNames));
            } catch (Exception e) {
                log.error("convert object error :{}", e);
            }
        }
        return list;
    }

    public static <T> T queryResultToObject(RowSet<Row> queryResult, Class<T> clazz, ModelMap resultMap) {
        List<String> columnNames = queryResult.columnsNames();
        RowIterator<Row> iterator = queryResult.iterator();
        if (columnNames != null && iterator.hasNext()) {
            try {
                return rowDataToObject(iterator.next(), clazz, resultMap, columnNames);
            } catch (Exception e) {
                log.error("convert object error :{}", e);
            }
        }
        return null;
    }

    public static Map<String, Object> queryResultToMap(RowSet<Row> queryResult, ModelMap resultMap) {
        List<String> columnNames = queryResult.columnsNames();
        RowIterator<Row> iterator = queryResult.iterator();
        if (columnNames != null && iterator.hasNext()) {
            return rowDataToMap(iterator.next(), resultMap, columnNames);
        }
        return new HashMap<>();
    }

    public static Map<String, Object> rowDataToMap(Row rowData, ModelMap resultMap, List<String> columnNames) {
        Map<String, Object> res = new HashMap<>();
        for (int index = 0; index < rowData.size(); index++) {
            String property = getProperty(resultMap, columnNames.get(index));
            res.put(property, getValue(rowData, index));
        }
        return res;
    }

    public static <T> T rowDataToObject(Row rowData, Class<T> clazz, ModelMap resultMap, List<String> columnNames) throws Exception {
        //只查一个字段或者count时，这时返回类型可能是String这种非用户自定义的类型
        // 所以这个类如果是JDK内的类（包括java.time里的时间类型），直接返回，因为它肯定不是用户自定义的那种model
        if (columnNames.size() == 1 && clazz.getClassLoader() == null) {
            Object item = getValue(rowData, 0);
            if (item == null) {
                return null;
            } else if (!clazz.equals(item.getClass())) {
                throw new ParseException(String.format("data type you want convert is %s ,but database return type is %s ,just change it", clazz, item.getClass()));
            } else {
                return (T) item;
            }
        }
        T t = clazz.getDeclaredConstructor().newInstance();
        for (int index = 0; index < rowData.size(); index++) {
            String property = getProperty(resultMap, columnNames.get(index));
            setProperty(clazz, t, property, getValue(rowData, index));
        }
        return t;
    }

    /**
     * 把驱动特有的类型转换成JDK类型：DECIMAL -> BigDecimal，BLOB/BINARY -> byte[]
     */
    private static Object getValue(Row row, int index) {
        Object value = row.getValue(index);
        if (value instanceof Numeric) {
            return ((Numeric) value).bigDecimalValue();
        }
        if (value instanceof Buffer) {
            return ((Buffer) value).getBytes();
        }
        return value;
    }

    private static void setProperty(Class clazz, Object object, String property, Object value) {
        Map<String, PropertyDescriptor> properties = classesWithProperty.get(clazz);
        if (properties == null) {
            synchronized (clazz) {
                if ((properties = classesWithProperty.get(clazz)) == null) {
                    properties = initpropertyDescriptors(clazz);
                    classesWithProperty.put(clazz, properties);
                }
            }
        }
        try {
            PropertyDescriptor propertyDescriptor = properties.get(property);
            if (propertyDescriptor == null) {
                log.error("can't find property: {}", property);
                return;
            }
            propertyDescriptor.setValue(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Map<String, PropertyDescriptor> initpropertyDescriptors(Class clazz) {
        Map<String, PropertyDescriptor> map = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            map.put(field.getName(), new PropertyDescriptor(clazz, field.getName()));
        }
        return map;
    }

    private static String getProperty(ModelMap resultMap, String columnName) {
        if (resultMap == null) {
            return columnName;
        }
        ColumnMapping resultMapping = resultMap.getColumnKeyMappings().get(columnName);
        if (resultMapping == null) {
            return columnName;
        }
        return resultMapping.getProperty();
    }
}
