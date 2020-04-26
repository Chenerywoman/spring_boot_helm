package com.techreturners.apiservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
<<<<<<< HEAD

import io.micrometer.core.annotation.Timed;


=======
import io.micrometer.core.annotation.Timed;

>>>>>>> 6358b8660e84942f0ed90de69e5013a7d113c882
@RestController
public class CourseDayController {

    Logger logger = LoggerFactory.getLogger(CourseDayController.class);

    @Autowired
    protected CourseDayService courseDayService = null;

<<<<<<< HEAD
	@GetMapping("/course-day")
    @Timed("get.course-day")
	public CourseDay getCourseDay(@RequestParam(defaultValue = "1") String day) {
        logger.info("getCourseDay() - START");
        CourseDay courseDay = courseDayService.getDay(Integer.parseInt(day));
         logger.info("getCourseDay() - END");
        return courseDay;
=======
    @GetMapping("/course-day")
    @Timed("get.course-day")
	public CourseDay getCourseDay(@RequestParam(defaultValue = "1") String day) {
        logger.info("getCourseDay() - START");
        CourseDay cd = courseDayService.getDay(Integer.parseInt(day));
        logger.info("getCourseDay() - END");
        return cd;
>>>>>>> 6358b8660e84942f0ed90de69e5013a7d113c882
	}
}



       
        
       
	