package com.mmd.marcobrico.config;


import org.springframework.stereotype.Component;

@Component
public class WeakPasswordChecker  {

//    private static final Set<String> WEAK_PASSWORDS = Set.of(
//            "123456", "password", "qwerty", "azerty", "mon_mot_de_passe123456"
//    );
//    @Override
//    public CompromisedPasswordDecision check(@Nullable String password) {
//        if (password == null) {
//            return new CompromisedPasswordDecision(true);
//        }
//        if (WEAK_PASSWORDS.contains(password)) {
//            return CompromisedPasswordDecision.compromised(
//                    "Password is in the weak/common list"
//            );
//        }
//        return CompromisedPasswordDecision.notCompromised();
//    }
}
