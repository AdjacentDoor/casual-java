/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.FieldType;
import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.mapper.CasualObjectMapper;
import se.laz.casual.api.buffer.type.fielded.mapper.PassThroughMapper;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedMarshallingException;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;
import se.laz.casual.api.util.FluentMap;
import se.laz.casual.api.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// sonar sucks understanding lambdas
@SuppressWarnings("squid:S1612")
public final class CommonDetails
{
    private static final Map<Class<?>, Class<?>> mapper;

    static
    {

        mapper = Collections.unmodifiableMap(FluentMap.of(new HashMap<Class<?>, Class<?>>())
                                                      .put(boolean.class, Boolean.class)
                                                      .put(byte.class, Byte.class)
                                                      .put(char.class, Character.class)
                                                      .put(double.class, Double.class)
                                                      .put(float.class, Float.class)
                                                      .put(int.class, Integer.class)
                                                      .put(long.class, Long.class)
                                                      .put(short.class, Short.class)
                                                      .put(void.class, Void.class)
                                                      .map());
    }


    private CommonDetails()
    {}

    public static List<Field> getCasuallyAnnotatedFields(final Class<?> c)
    {
        return Arrays.stream(c.getDeclaredFields())
                     .filter(f -> hasCasualFieldAnnotation(f))
                     .collect(Collectors.toList());
    }


    public static List<Method> getCasuallyAnnotatedMethods(final Class<?> c)
    {
        return Arrays.stream(c.getMethods())
                     .filter(m -> hasCasualFieldAnnotation(m))
                     .collect(Collectors.toList());
    }

    public static Map<Method, List<ParameterInfo>> getParameterInfo(final Class<?> c)
    {
        List<Method> methods = Arrays.stream(c.getMethods())
                                     .collect(Collectors.toList());
        Map<Method, List<ParameterInfo>> map = new HashMap<>();
        for(Method m : methods)
        {
            List<ParameterInfo> l = getParameterInfo(m);
            if(!l.isEmpty())
            {
                map.put(m, l);
            }
        }
        return map;
    }

    public static List<ParameterInfo> getParameterInfo(final Method m)
    {
        List<Type> genericParameterTypes = Arrays.stream(m.getGenericParameterTypes()).collect(Collectors.toList());
        List<ParameterInfo> parameterInfo = new ArrayList<>();
        Parameter[] parameters = m.getParameters();
        for(int i = 0; i < m.getParameterCount(); ++i)
        {
            Parameter p = parameters[i];
            Optional<Annotation> a = Arrays.stream(p.getDeclaredAnnotations())
                                           .filter(v -> v instanceof CasualFieldElement)
                                           .findFirst();
            if(a.isPresent())
            {
                boolean isParameterizedType = genericParameterTypes.get(i) instanceof ParameterizedType;
                parameterInfo.add(AnnotatedParameterInfo.of(a.get(), p.getType(), isParameterizedType ? Optional.of((ParameterizedType)genericParameterTypes.get(i)) : Optional.empty()));
            }
            else
            {
                // if there's no annotation and the parameter type itself does not contain any @CasualFieldElement
                // then we just bail and continue
                if(!hasCasualFieldAnnotation(p.getType()))
                {
                    return new ArrayList<>();
                }
                parameterInfo.add(ParameterInfo.of(p.getType()));
            }
        }
        return parameterInfo;
    }

    private static boolean hasCasualFieldAnnotation(final Class<?> type)
    {
        for(Field f : type.getDeclaredFields())
        {
            if(hasCasualFieldAnnotation(f))
            {
                return true;
            }
        }
        for(Method m : type.getDeclaredMethods())
        {
            if(hasCasualFieldAnnotation(m))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean hasCasualFieldAnnotation(final Field f)
    {
        return null != f.getAnnotation(CasualFieldElement.class);
    }

    public static boolean hasCasualFieldAnnotation(final Method m)
    {
        return null != m.getAnnotation(CasualFieldElement.class);
    }

    public static boolean isArrayType(Class<?> o)
    {
        return o.isArray();
    }

    public static boolean isListType(Class<?> o)
    {
        return o.isAssignableFrom(ArrayList.class);
    }

    public static boolean isFieldedType(Class<?> c)
    {
        return FieldType.isOfFieldType(c);
    }

    public static Class<?> wrapIfPrimitive(Class<?> c)
    {
        return c.isPrimitive() ? mapper.get(c) : c;
    }

    public static Object adaptValueToFielded(Object v)
    {
        // integers are transported as long
        return v.getClass().equals(Integer.class) ? Long.valueOf((int)v) : v;
    }

    public static Class<?> adaptTypeToFielded(Class<?> clazz)
    {
        return clazz.equals(Integer.class) ? Long.class : clazz;
    }

    public static Optional<String> getListLengthName(final CasualFieldElement annotation)
    {
        return annotation.lengthName().isEmpty() ? Optional.empty() : Optional.of(annotation.lengthName());
    }

    // squid:S1452 - generic wildcard
    @SuppressWarnings({"unchecked", "squid:S1452"})
    public static Optional<Function<Object, ? extends Object>> getMapperTo(CasualFieldElement annotation)
    {
        final CasualObjectMapper<Object, ? extends Object> instance;
        try
        {
            instance = (CasualObjectMapper<Object, ? extends Object>)annotation.mapper().newInstance();
            return instance.getClass().equals(PassThroughMapper.class) ? Optional.empty() : Optional.of(v -> instance.to(v));
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new FieldedMarshallingException("can not get object mapper for: " + annotation, e);
        }
    }

    // squid:S1452 - generic wildcard
    @SuppressWarnings({"unchecked", "squid:S1452"})
    public static Optional<Pair<Function<Object, ? extends Object>, Class<?>>> getMapperFrom(CasualFieldElement annotation)
    {
        final CasualObjectMapper<? extends Object, Object> instance;
        try
        {
            instance = (CasualObjectMapper<? extends Object, Object>)annotation.mapper().newInstance();
            return instance.getClass().equals(PassThroughMapper.class) ? Optional.empty() : Optional.of(Pair.of(v -> instance.from(v), instance.getDstType()));
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new FieldedUnmarshallingException("can not get object mapper for: " + annotation, e);
        }
    }

}