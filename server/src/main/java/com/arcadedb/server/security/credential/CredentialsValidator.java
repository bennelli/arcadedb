/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
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
package com.arcadedb.server.security.credential;

import com.arcadedb.server.security.ServerSecurityException;

/**
 * Interface for validating credentials. The default implementation is @{@link DefaultCredentialsValidator}.
 *
 * @author Luca Garulli (l.garulli@arcadedata.com)
 */
public interface CredentialsValidator {
  /**
   * Validates user and password. In case of validation issues, this method throws a ServerSecurityException exception.
   *
   * @throws ServerSecurityException
   */
  void validateCredentials(final String userName, final String userPassword) throws ServerSecurityException;

  String generateRandomPassword();
}
