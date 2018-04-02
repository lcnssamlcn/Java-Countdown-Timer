JAVAC = javac
JAVA = java
JAR = jar

SRC_RAW = Main.java MainWindow.java Timer.java
SRC_DIR = src

AUDIO_RAW = sound.wav
AUDIO_DIR = sound

OBJ_RAW = $(SRC_RAW:.java=.class)
OBJDIR = objects

# $OS (%OS%) is only defined in Windows
# $OS in Linux, Mac is empty string
ifeq ($(OS),Windows_NT)
	SRC = $(addprefix $(SRC_DIR)/,$(SRC_RAW))
	AUDIO = $(addprefix $(AUDIO_DIR)/,$(AUDIO_RAW))
	OBJ = $(addprefix $(OBJDIR)/,$(OBJ_RAW))
else 
	UNAME = $(shell uname)
	ifneq (,$(findstring $(UNAME),Linux Darwin))  # replacing logical OR
		SRC = $(addprefix $(SRC_DIR)/,$(SRC_RAW))
		AUDIO = $(addprefix $(AUDIO_DIR)/,$(AUDIO_RAW))
		OBJ = $(addprefix $(OBJDIR)/,$(OBJ_RAW))
	endif
endif
	
JAR_RAW = countdown_timer.jar
MAIN_CLASS = objects/Main.class

ROOT_DIR = $(realpath .)
ifeq ($(OS),Windows_NT)
	CLASSPATH = "$(addprefix $(ROOT_DIR)/,$(AUDIO_DIR));$(addprefix $(ROOT_DIR)/,$(OBJDIR))"
	JAR_CLASSPATH = "$(addprefix $(ROOT_DIR)/,$(JAR_RAW));$(AUDIO_DIR);$(OBJDIR)"
else
	UNAME = $(shell uname)
	ifneq (,$(findstring $(UNAME),Linux Darwin))
		CLASSPATH = "$(addprefix $(ROOT_DIR)/,$(AUDIO_DIR)):$(addprefix $(ROOT_DIR)/,$(OBJDIR))"
		JAR_CLASSPATH = "$(addprefix $(ROOT_DIR)/,$(JAR_RAW)):$(AUDIO_DIR):$(OBJDIR)"
	endif
endif


all: $(SRC) $(AUDIO) obj_dir timer

# object directory would not be created by javac command => self-create
obj_dir: 
	@if [ ! -d "$(OBJDIR)" ]; then \
		echo "Creating Object Directory..."; \
		mkdir $(OBJDIR); \
	fi

timer:
	$(JAVAC) -cp $(CLASSPATH) $(SRC) -d $(OBJDIR)

jar: $(AUDIO) $(OBJ)
	$(JAR) cvf $(JAR_RAW) $(AUDIO_DIR) $(OBJDIR)
	$(JAR) ufe $(JAR_RAW) Main $(MAIN_CLASS)

run: $(OBJ)
	$(JAVA) -cp $(CLASSPATH) Main

run_jar: $(JAR_RAW)
	$(JAVA) -cp $(JAR_CLASSPATH) Main

test:
	@echo classpath: $(CLASSPATH)
	@echo jar classpath: $(JAR_CLASSPATH)
	@#echo current dir: $(realpath .)

.PHONY: clean
clean:
	@if [ -d "$(OBJDIR)" ]; then \
		echo "Cleaning Object Directory..."; \
		rm -R $(OBJDIR); \
	fi
	@if [ -e "$(JAR_RAW)" ]; then \
		echo "Cleaning JAR..."; \
		rm $(JAR_RAW); \
	fi
