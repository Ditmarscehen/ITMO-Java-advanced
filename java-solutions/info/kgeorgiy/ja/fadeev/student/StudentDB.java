package info.kgeorgiy.ja.fadeev.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return get(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return get(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return get(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return get(students, getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortBy(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortBy(students, comparatorByName);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filteredStreamBy(students, group, Student::getGroup)
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    private <T> List<T> get(List<Student> students, Function<Student, T> function) {
        return students.stream().map(function).collect(Collectors.toList());
    }

    private List<Student> sortBy(Collection<Student> students, Comparator<Student> comparator) {
        return sortStreamBy(students.stream(), comparator);
    }

    private List<Student> sortStreamBy(Stream<Student> students, Comparator<Student> comparator) {
        return students.sorted(comparator).collect(Collectors.toList());
    }

    private <T> List<Student> findStudentsBy(Collection<Student> students, T parameter, Function<Student, T> function) {
        return sortStreamBy(filteredStreamBy(students, parameter, function), comparatorByName);
    }

    private <T> Stream<Student> filteredStreamBy(Collection<Student> students, T parameter, Function<Student, T> function) {
        return students.stream().filter(student -> function.apply(student).equals(parameter));
    }

    // :NOTE: why not static?
    private static final Function<Student, String> getFullName = s -> s.getFirstName() + " " + s.getLastName();

    // this as well
    private static final Comparator<Student> comparatorByName =
            Comparator.comparing(Student::getLastName, Comparator.reverseOrder())
                    .thenComparing(Student::getFirstName, Comparator.reverseOrder())
                    .thenComparing(Student::getId);
}