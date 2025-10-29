package com.znzlspt.server.service;

import com.znzlspt.netcore.command.Command;
import com.znzlspt.server.command.Echo;
import com.znzlspt.server.command.Say;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CommandUtil {

    Map<Short, Class<? extends Command>> functions = new HashMap<>();

    public void init() {
        functions.clear();
        functions.put((short) 100, Echo.class);
        functions.put((short) 101, Say.class);
    }


    /**
     * Reflection 을 적용해서
     * Class 객체를 통해 Map에 저장된 키 값을 커맨드로 조회하여 commandService 에 주입될 클래스의 이름, 패키지, 상위 클래스, 인터페이스 정보등을 동적으로 가져올수 있습니다.
     * @param command
     * @return
     */
    public CommandServiceImpl findFunction(short command) {

        Class<? extends CommandServiceImpl> clazz;
        CommandServiceImpl commandServiceImpl;
        try {
            clazz = functions.get(command);
            if (clazz == null) {
                throw new IllegalArgumentException("Unsupported command id: " + command);
            }
            commandServiceImpl = clazz.getDeclaredConstructor().newInstance();
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

    public Map<Short, Class<? extends CommandServiceImpl>> getFunctions() {
        return functions;
    }
}
