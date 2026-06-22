Feature: Manage access levels
  Uczelniany System Antyplagiatowy -
  As a system administrator
  I want to grant and revoke access levels for user accounts
  So that I can manage roles such as STUDENT, TEACHER and ADMIN in the system

  Background:
    Given the API application is running and properly configured
    And I am logged in as an administrator

  Scenario: 1. Add TEACHER access level
    When I grant access level "TEACHER" to account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 200 status code
    And the response must not be empty
    And the response should contain access level "TEACHER"

  Scenario: 2. Remove TEACHER access level
    Given account "a0000000-0000-0000-0000-000000000002" already has access level "TEACHER"
    When I revoke access level "TEACHER" from account with UUID "a0000000-0000-0000-0000-000000000002"
    Then I receive a 200 status code

  Scenario Outline: 3-4. Add access level to not existing accounts
    When I grant access level "STUDENT" to account with UUID "<accountUUID>"
    Then I receive a <statusCode> status code

    Examples:
      | accountUUID                          | statusCode |
      | 99999999-9999-9999-9999-999999999999 | 404        |
      | 00000000-0000-0000-0000-000000000000 | 404        |

  Scenario Outline: 5-6. Remove access level from not existing accounts
    When I revoke access level "TEACHER" from account with UUID "<accountUUID>"
    Then I receive a <statusCode> status code

    Examples:
      | accountUUID                          | statusCode |
      | 99999999-9999-9999-9999-999999999999 | 404        |
      | 00000000-0000-0000-0000-000000000000 | 404        |

  Scenario: 7. Add access level without authorization
    Given I am not logged in
    When I grant access level "TEACHER" to account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 401 status code

  Scenario: 8. Remove access level without authorization
    Given I am not logged in
    When I revoke access level "TEACHER" from account with UUID "a0000000-0000-0000-0000-000000000002"
    Then I receive a 401 status code

  Scenario: 9. Revoke access level from an account without such access level
    When I revoke access level "ADMIN" from account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 409 status code

  Scenario: 10. Add not existing access level to user
    When I grant access level "IAS64" to account with UUID "a0000000-0000-0000-0000-000000000003"
    Then I receive a 409 status code
    And the response must not be empty
