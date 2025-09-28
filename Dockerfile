# 실행 전용 이미지 (JRE)
FROM eclipse-temurin:21-jre

ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul"

WORKDIR /app
COPY app.jar .

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
