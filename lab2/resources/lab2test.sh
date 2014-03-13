#/bin/bash
#author: Prateek Sharma  prateeks@cs.umass.edu
#Lab 2 checking script
# Checks unzipping, directory structure, locates the executables, and runs the program


echo "Usage : ./lab2test.sh yourname.zip"

echo "Unzipping file .. $1 "
unzip $1 
if [ $? -ne 0 ]; then
    echo "Nopes, cant unzip"
fi
DIRNAME=`echo $1 | sed 's/\.zip//g'`

echo "cd'ing to $DIRNAME"
cd $DIRNAME
if [ $? -ne 0 ]; then
    echo "Nopes, cant CD into directory. Probably a wrongly created zip file."
fi

echo "Checking README.txt"
if [ ! -f "README.txt" ]; then
    echo "README.txt doesn't exist"
fi

echo "Checking GROUP.txt"
if [ ! -f "GROUP.txt" ]; then
    echo "ERR:GROUP.txt doesn't exist"
fi

echo "Checking OUTPUT.txt"
if [ ! -f "OUTPUT.txt" ]; then
    echo "ERR:OUTPUT.txt doesn't exist"
fi


echo "Checking Index.class or bin/Index.class" 
EXPATH=""
if [ -f "Index.class" ]; then
    EXPATH="Index.class"

elif [ -f "bin/Index.class" ]; then
    EXPATH="bin/Index.class"
else 
echo "ERR:Index.class not found, exiting"
exit 1
fi

# TODO, use find to locate Index.class

echo "Running Your program with java $EXPATH 5 file1.txt"


