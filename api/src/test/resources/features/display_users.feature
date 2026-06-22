Feature: Display users in a system
  \Uczelniany System Antyplagiatowy -
  As a system administrator
  I want to retrieve account details by UUID or login
  So that I can inspect user data and access levels at any time

  Background:
    Given the API application is running and properly configured
    And I am logged in as an administrator

  Scenario: 1. Get user account by UUID
    When I fetch account details by UUID "a0000000-0000-0000-0000-000000000001"
    Then I receive a 200 status code
    And the response must not be empty
    And the response should contain login "test_admin"

  Scenario: 2.  Get not existing user account by UUID
    When I fetch account details by UUID "99999999-9999-9999-9999-999999999999"
    Then I receive a 404 status code

  Scenario: 3.  Get user account by login
    When I fetch account details by login "test_teacher"
    Then I receive a 200 status code
    And the response must not be empty
    And the response should contain login "test_teacher"

  Scenario: 4. Get not existing user account by login
    When I fetch account details by login "login_ktory_nie_istnieje"
    Then I receive a 404 status code

  Scenario: 5. Get all accounts
    When I fetch the list of all accounts
    Then I receive a 200 status code
    And the response must not be empty

  Scenario: 6. Get accounts without authorization
    Given I am not logged in
    When I fetch account details by UUID "a0000000-0000-0000-0000-000000000001"
    Then I receive a 401 status code

  Scenario Outline: 7-8. Get user account by not existing UUID
    When I fetch account details by UUID "<UUID>"
    Then I receive a <statusCode> status code

    Examples:
      | UUID      | statusCode |
      | wrong-uuid   | 400        |
      | 123          | 400        |
