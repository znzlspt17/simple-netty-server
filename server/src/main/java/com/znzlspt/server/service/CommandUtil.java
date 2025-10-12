package com.znzlspt.server.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CommandUtil {

    Map<Short, String> functions = new HashMap<>();

    public void init() {
        functions.put((short) 100, "com.cocoz.command.function.Echo");
        functions.put((short) 101, "com.cocoz.command.function.Say");
    }


    /**
     * Reflection 을 적용해서
     * Class 객체를 통해 Map에 저장된 키 값을 커맨드로 조회하여 commandService 에 주입될 클래스의 이름, 패키지, 상위 클래스, 인터페이스 정보등을 동적으로 가져올수 있습니다.
     * @param command
     * @return
     */
    public CommandServiceImpl findFunction(short command) {

        String funcName = functions.get(command);
        Class<?> clazz;
        CommandServiceImpl commandServiceImpl;
        try {
            clazz = Class.forName(funcName);
            commandServiceImpl = (CommandServiceImpl) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e1) {
            throw new RuntimeException(e1);
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(e2);
        } catch (InstantiationException e3) {
            throw new RuntimeException(e3);
        } catch (IllegalAccessException e4) {
            throw new RuntimeException(e4);
        } catch (NoSuchMethodException e5) {
            throw new RuntimeException(e5);
        }
        return commandServiceImpl;
    }

    public Map<Short, String> getFunctions() {
        return functions;
    }
}
