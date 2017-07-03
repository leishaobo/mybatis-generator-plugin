/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.mybatis.generator.plugins.tools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/28 14:26
 * ---------------------------------------------------------------------------
 */
public class ObjectUtil {
    private Object object;  // 对象
    private Class cls;  // 类

    /**
     * 构造函数(枚举#分隔)
     * @param loader
     * @param cls
     */
    public ObjectUtil(ClassLoader loader, String cls) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (cls.indexOf("#") == -1){
            this.cls = loader.loadClass(cls);
            this.object = this.cls.newInstance();
        } else {
            String[] strs = cls.split("#");
            this.cls = loader.loadClass(strs[0]);
            if (this.cls.isEnum()){
                Object[] constants = this.cls.getEnumConstants();
                for (Object object : constants){
                    ObjectUtil eObject = new ObjectUtil(object);
                    if (strs[1].equals(eObject.get("name"))){
                        this.object = object;
                        break;
                    }
                }
                System.out.println("");
            } else {
                throw new ClassNotFoundException("没有找到对应枚举" + strs[0]);
            }
        }
    }

    /**
     * 构造函数
     * @param object
     */
    public ObjectUtil(Object object) {
        this.object = object;
        this.cls = object.getClass();
    }

    /**
     * 设置值
     * @param filedName
     * @param value
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public ObjectUtil set(String filedName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = this.getDeclaredField(filedName);
        field.setAccessible(true);
        field.set(this.object, value);
        return this;
    }

    /**
     * 获取值
     *
     * @param filedName
     * @return
     * @throws IllegalAccessException
     */
    public Object get(String filedName) throws IllegalAccessException {
        Field field = this.getDeclaredField(filedName);
        field.setAccessible(true);
        return field.get(this.object);
    }

    /**
     * Getter method for property <tt>object</tt>.
     * @return property value of object
     * @author hewei
     */
    public Object getObject() {
        return object;
    }

    /**
     * 执行方法
     * @param methodName
     * @param args
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method[] methods = this.cls.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length) {
                boolean flag = true;
                Class[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < args.length; i++) {
                    if (!(parameterTypes[i].isAssignableFrom(args[i].getClass()))) {
                        flag = false;
                    }
                }

                if (flag) {
                    return method.invoke(this.object, args);
                }
            }
        }
        return null;
    }

    /**
     * 执行方法(mapper动态代理后VarArgs检查有问题)
     * @param methodName
     * @param args
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invokeVarArgs(String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method[] methods = this.cls.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method.invoke(this.object, args);
            }
        }
        return null;
    }

    /**
     * Getter method for property <tt>cls</tt>.
     * @return property value of cls
     * @author hewei
     */
    public Class getCls() {
        return cls;
    }

    /**
     * 递归获取所有属性
     *
     * @param name
     * @return
     */
    private Field getDeclaredField(String name) {
        Class<?> clazz = this.cls;
        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e){
                // 不能操作，递归父类
            }
        }
        return null;
    }
}
