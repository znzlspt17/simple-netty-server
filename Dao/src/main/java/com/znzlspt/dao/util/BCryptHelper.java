package com.znzlspt.dao.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptHelper {
    private BCryptHelper() {}

    public static String hashPassword(String password){
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verifyPassword(String password, String hashed){
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashed);
        return result.verified;
    }
}
