package com.tg.async.dynamic.xmltags;

import ognl.MemberAccess;
import ognl.OgnlContext;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Only public members are accessible from OGNL expressions; nothing is made accessible via reflection.
 */
public class OgnlMemberAccess implements MemberAccess {

    @Override
    public Object setup(OgnlContext context, Object target, Member member, String propertyName) {
        return null;
    }

    @Override
    public void restore(OgnlContext context, Object target, Member member, String propertyName, Object state) {
    }

    @Override
    public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
        return Modifier.isPublic(member.getModifiers());
    }
}
