package ru.javawebinar.topjava.web.meal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.javawebinar.topjava.service.MealService;

public abstract class AbstractMealController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MealService service;
}