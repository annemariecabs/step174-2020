// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

/**
 * Creates a custom error to be thrown when an API is not available
 */
public class APINotAvailableException extends Exception {
  /** holds the error message for the exception */
  private final String errorMessage;

  /**
   * Constructs a custom exception for when API is not available
   * with a specified error message.
   *
   * @param errorMessage the error message for this exception
   */
  public APINotAvailableException(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Returns a String representation of this exception.
   *
   * @return "APINotAvailableException occurred: " with the passed-in error message.
   */
  public String toString() {
    return "APINotAvailableException occurred: " + errorMessage;
  }
}
