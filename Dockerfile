FROM openjdk:8-jdk-alpine

COPY ./udemy-course-review-collector-1.0.0.jar .

CMD ["java", "-jar", "udemy-course-review-collector-1.0.0.jar"]