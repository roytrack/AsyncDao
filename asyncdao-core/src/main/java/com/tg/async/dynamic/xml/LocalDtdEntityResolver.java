package com.tg.async.dynamic.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;

/**
 * Resolves the MyBatis mapper DTD from the bundled classpath copy and refuses to load any other external entity,
 * so parsing a mapper never goes to the network and is not exposed to XXE.
 */
public class LocalDtdEntityResolver implements EntityResolver {
    private static final String MAPPER_DTD = "mybatis-3-mapper.dtd";

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId != null && systemId.toLowerCase().endsWith(MAPPER_DTD)) {
            InputStream in = LocalDtdEntityResolver.class.getClassLoader().getResourceAsStream(MAPPER_DTD);
            if (in != null) {
                InputSource source = new InputSource(in);
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            }
        }
        return new InputSource(new StringReader(""));
    }
}
