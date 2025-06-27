# 실행 전용 이미지이므로 멀티-스테이지 없이 JRE만 사용
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY app.jar .

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]