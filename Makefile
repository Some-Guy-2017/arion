.PHONY: all jar run clean git-update

# variables to specify
BUILD_DIR := ./build/
SRC_DIR := ./src/
MAIN_CLASS := arion.Arion
TEST_CLASSES := unit_test.DatabaseTests unit_test.FlashcardTests unit_test.ArionTests unit_test.ArionDisplayTests 
JAR_FILE := arion.jar

SRCS := $(shell find ${SRC_DIR} -name *.java)
BINS := $(SRCS:${SRC_DIR}%.java=${BUILD_DIR}%.class)
REQUIRED_FILES := Makefile

JAVA_FLAGS := -classpath ${BUILD_DIR}
JAR_FLAGS := --create --file=${JAR_FILE} --main-class=${MAIN_CLASS} -C ${BUILD_DIR}
COMPILE_FLAGS := -sourcepath ${SRC_DIR} -d ${BUILD_DIR} -Xlint:unchecked
TEST_FLAGS := -ea ${JAVA_FLAGS}

all: ${BINS}

jar: all
	jar ${JAR_FLAGS} .

run: all
	java ${JAVA_FLAGS} ${MAIN_CLASS}

test: all
	$(foreach class,${TEST_CLASSES},java ${TEST_FLAGS} "${class}";)

clean:
	rm -rf ${BUILD_DIR}
	rm -f ${JAR_FILE}

${BUILD_DIR}%.class: ${SRC_DIR}%.java ${REQUIRED_FILES}
	javac ${COMPILE_FLAGS} $<
