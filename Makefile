.PHONY: all jar run clean git-update

# variables to specify
BUILD_DIR := ./build
SRC_DIR := ./src
MAIN_CLASS = arion.Arion
#MAIN_CLASS = unit_test.ArionDisplayTests
JAR_FILE := arion.jar

SRCS := $(shell find ${SRC_DIR} -name *.java)
BINS := $(SRCS:${SRC_DIR}/%.java=${BUILD_DIR}/%.class)
REQUIRED_FILES := Makefile


all: ${BINS}

jar: all
	jar --create --file=${JAR_FILE} --main-class=${MAIN_CLASS} -C ${BUILD_DIR} .

run: all
	java -classpath ${BUILD_DIR} ${MAIN_CLASS}

clean:
	@rm -rf ${BUILD_DIR}
	@rm -f ${JAR_FILE}

${BUILD_DIR}/%.class: ${SRC_DIR}/%.java ${REQUIRED_FILES}
	javac -sourcepath ${SRC_DIR} -d ${BUILD_DIR} $<
