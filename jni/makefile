# Define a variable for classpath
CLASS_PATH = ../bin/codec

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)

all : Codec.dll

# $@ matches the target, $< matches the first dependancy
# link 맨뒤에 안넣으니까 안됨
Codec.dll : Codec.o
	gcc -L"D:\eclipseWorkspace\ffmpeg-3.4.1-win64-dev\ffmpeg-3.4.1-win64-dev\lib" -shared -m64 -Wl,--add-stdcall-alias -o $@ $< -lavcodec -lavformat -lavutil

# $@ matches the target, $< matches the first dependancy
Codec.o : codec_Codec.c codec_Codec.h
	gcc -m64 -I"C:\Program Files\Java\jdk1.8.0_151\include" -I"C:\Program Files\Java\jdk1.8.0_151\include\win32" -I"D:\eclipseWorkspace\ffmpeg-3.4.1-win64-dev\ffmpeg-3.4.1-win64-dev\include" -c $< -o $@

clean :
	rm codec_Codec.h Codec.o Codec.dll