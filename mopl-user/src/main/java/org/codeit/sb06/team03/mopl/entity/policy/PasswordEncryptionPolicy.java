package org.codeit.sb06.team03.mopl.entity.policy;

import org.codeit.sb06.team03.mopl.entity.vo.Password;

public interface PasswordEncryptionPolicy {

    Password apply(String rawPassword);
}
