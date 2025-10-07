package com.reliaquest.api.testdata;

import com.reliaquest.api.model.Employee;
import java.util.List;

public class EmployeeTestData {

    public static final Employee ALICE = new Employee("1", "Alice", 1000, 30, "Proj. Mgr", "alice@test.com");
    public static final Employee BOB = new Employee("2", "Bob", 900, 25, "Dev", "bob@test.com");

    public static List<Employee> allEmployees() {
        return List.of(ALICE, BOB);
    }
}
