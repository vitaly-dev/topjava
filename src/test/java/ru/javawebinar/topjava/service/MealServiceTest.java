package ru.javawebinar.topjava.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.MealTestData.NOT_FOUND;
import static ru.javawebinar.topjava.MealTestData.getNew;
import static ru.javawebinar.topjava.MealTestData.getUpdated;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.*;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml"
})
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class MealServiceTest {

    @Autowired
    private MealService service;

    @Test
    public void delete() {
        service.delete(MEAL1_ID, USER_ID);
        assertThrows(NotFoundException.class, () -> service.get(MEAL1_ID, USER_ID));
    }

    @Test
    public void deleteNotFound() {
        assertThrows(NotFoundException.class, () -> service.delete(NOT_FOUND, USER_ID));
    }

    @Test
    public void deleteNotOwn() {
        assertThrows(NotFoundException.class, () -> service.delete(MEAL1_ID, ADMIN_ID));
    }

    @Test
    @Transactional
    public void create() {
        Meal created = service.create(getNew(user), USER_ID);
        int newId = created.id();
        Meal newMeal = getNew(user);
        newMeal.setId(newId);
        MEAL_MATCHER_IGNORING.assertMatch(created, newMeal);
        MEAL_MATCHER_IGNORING.assertMatch(service.get(newId, USER_ID), newMeal);
    }

    @Test
    public void duplicateDateTimeCreate() {
        assertThrows(DataAccessException.class, () ->
                service.create(new Meal(null, meal1.getDateTime(), "duplicate", 100), USER_ID));
    }

    @Test
    @Transactional
    public void get() {
        Meal actual = service.get(ADMIN_MEAL_ID, ADMIN_ID);
        MEAL_MATCHER_IGNORING.assertMatch(actual, adminMeal1);
    }

    @Test
    public void getNotFound() {
        assertThrows(NotFoundException.class, () -> service.get(NOT_FOUND, USER_ID));
    }

    @Test
    public void getNotOwn() {
        assertThrows(NotFoundException.class, () -> service.get(MEAL1_ID, ADMIN_ID));
    }

    @Test
    @Transactional
    public void update() {
        Meal updated = getUpdated(user);
        service.update(updated, USER_ID);
        MEAL_MATCHER_IGNORING.assertMatch(service.get(MEAL1_ID, USER_ID), getUpdated(user));
    }

    @Test
    @Transactional
    public void updateNotOwn() {
        assertThrows(NotFoundException.class, () -> service.update(meal1, ADMIN_ID));
        MEAL_MATCHER_IGNORING.assertMatch(service.get(MEAL1_ID, USER_ID), meal1);
    }

    @Test
    @Transactional
    public void getAll() {
        MEAL_MATCHER_IGNORING.assertMatch(service.getAll(USER_ID), meals);
    }

    @Test
    @Transactional
    public void getBetweenInclusive() {
        MEAL_MATCHER_IGNORING.assertMatch(service.getBetweenInclusive(
                        LocalDate.of(2020, Month.JANUARY, 30),
                        LocalDate.of(2020, Month.JANUARY, 30), USER_ID),
                meal3, meal2, meal1);
    }

    @Test
    @Transactional
    public void getBetweenWithNullDates() {
        MEAL_MATCHER_IGNORING.assertMatch(service.getBetweenInclusive(null, null, USER_ID), meals);
    }
}