.PHONY: all jar run clean git-update

# variables to specify
BUILD_DIR := ./build/
SRC_DIR := ./src/
MAIN_CLASS := arion.Arion
JAR_FILE := arion.jar

SRCS := $(shell find ${SRC_DIR} -name *.java)
BINS := $(SRCS:${SRC_DIR}%.java=${BUILD_DIR}%.class)
REQUIRED_FILES := Makefile

JAVA_FLAGS := -classpath ${BUILD_DIR} -ea
JAR_FLAGS := --create --file=${JAR_FILE} --main-class=${MAIN_CLASS} -C ${BUILD_DIR}
COMPILE_FLAGS := -sourcepath ${SRC_DIR} -d ${BUILD_DIR} -Xlint:unchecked

all: ${BINS}

jar: all
	jar ${JAR_FLAGS} .

run: all
	java ${JAVA_FLAGS} ${MAIN_CLASS}

clean:
	rm -rf ${BUILD_DIR}
	rm -f ${JAR_FILE}

${BUILD_DIR}%.class: ${SRC_DIR}%.java ${REQUIRED_FILES}
	javac ${COMPILE_FLAGS} $<
