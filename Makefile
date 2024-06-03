.PHONY: all jar run clean git-update

# variables to specify
BUILD_DIR := ./build/
SRC_DIR := ./src/
MAIN_CLASS := arion.Arion
#TEST_CLASSES := unit_test.ArionDisplayTests unit_test.DatabaseTests unit_test.ArionTests unit_test.FlashcardTests
#TEST_CLASSES := unit_test.FlashcardTests
TEST_CLASSES := unit_test.DatabaseTests
JAR_FILE := arion.jar

SRCS := $(shell find ${SRC_DIR} -name *.java)
BINS := $(SRCS:${SRC_DIR}%.java=${BUILD_DIR}%.class)
REQUIRED_FILES := Makefile

JAVA_FLAGS := -classpath ${BUILD_DIR}


all: ${BINS}

jar: all
	jar --create --file=${JAR_FILE} --main-class=${MAIN_CLASS} -C ${BUILD_DIR} .

run: all
	java ${JAVA_FLAGS} ${MAIN_CLASS}

test: all ${TEST_CLASSES}

unit_test.%:
	java ${JAVA_FLAGS} -ea $@

clean:
	@rm -rf ${BUILD_DIR}
	@rm -f ${JAR_FILE}

${BUILD_DIR}%.class: ${SRC_DIR}%.java ${REQUIRED_FILES}
	javac -sourcepath ${SRC_DIR} -d ${BUILD_DIR} $<
