Feature: Uczelniany System Antyplagiatowy
  As a system administrator
  I want to block and unblock user accounts
  So that I can say that the system is working properly

  Background:
    Given the API application is running and properly configured
    And I am logged in as an administrator

  Scenario: 1. Block user account
    When I send a request to block the account with UUID "a0000000-0000-0000-0000-000000000002"
    Then I receive a 204 status code

  Scenario Outline: 2-3. Not existing accounts - block
    When I send a request to block the account with UUID "<accountUUID>"
    Then I receive a <statusCode> status code

    Examples:
      | accountUUID                          | statusCode |
      | 99999999-9999-9999-9999-999999999999 | 404        |
      | 00000000-0000-0000-0000-000000000000 | 404        |

  Scenario Outline: 4-5. Not existing accounts - unblock
    When I send a request to unblock the account with UUID "<accountUUID>"
    Then I receive a <statusCode> status code

    Examples:
      | accountUUID                          | statusCode |
      | 99999999-9999-9999-9999-999999999999 | 404        |
      | 00000000-0000-0000-0000-000000000000 | 404        |

  Scenario: 6. Block user account without authorization
    Given I am not logged in
    When I send a request to block the account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 401 status code

  Scenario: 7. Unblock user account without authorization
    Given I am not logged in
    When I send a request to unblock the account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 401 status code

  Scenario: 8. Blocking already blocked account result in 409 CONFLICT
    When I send a request to block the account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 204 status code
    When I send a request to block the account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 409 status code

  Scenario: 9. Request with not valid uuid
    When I send a request to block the account with UUID "wrong"
    Then I receive a 400 status code
    And the response must not be empty
