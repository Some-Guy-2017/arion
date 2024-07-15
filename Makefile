.PHONY: all jar run clean git-update

BUILD_DIR := ./build/
SRC_DIR := ./src/
RES_DIR := ./res/
MAIN_CLASS := arion.Arion
JAR_FILE := arion.jar

SRCS := $(shell find ${SRC_DIR} -name *.java)
BINS := $(SRCS:${SRC_DIR}%.java=${BUILD_DIR}%.class)
RESOURCES := $(shell find ${RES_DIR})

JAVA_FLAGS := -classpath ${BUILD_DIR} -ea
JAR_FLAGS := --create --file=${JAR_FILE} --main-class=${MAIN_CLASS} -C ${BUILD_DIR}
COMPILE_FLAGS := -sourcepath ${SRC_DIR} -d ${BUILD_DIR} -Xlint:unchecked

COMMON_DEPS := Makefile

all: ${BUILD_DIR} ${BINS} ${BUILD_DIR}/${RES_DIR}

jar: all
	jar ${JAR_FLAGS} .

run: all
	java ${JAVA_FLAGS} ${MAIN_CLASS}

clean:
	rm -rf ${BUILD_DIR}
	rm -f ${JAR_FILE}
	rm -f ./flashcards.txt
	rm -f ./log.txt

${BUILD_DIR}%.class: ${SRC_DIR}%.java ${COMMON_DEPS}
	javac ${COMPILE_FLAGS} $<

${BUILD_DIR}:
	rm -rf $@
	mkdir $@

${BUILD_DIR}/${RES_DIR}: ${RES_DIR} ${RESOURCES} ${COMMON_DEPS}
	rm -rf $@
	cp -r $< $@
