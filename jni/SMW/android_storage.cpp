/*
 * android_storage.cpp
 *
 *  Created on: Jun 30, 2012
 *      Author: lithium
 */
#include <iostream>
#include <fstream>
#include <string>
#include "android_storage.h"

using namespace std;

char storage_dir[128] = "";

const char * getStorageDir(void)
{
	string line;
	ifstream file(STORAGE_INFO_FILE);
	if (file.is_open())
	{
		while(file.good())
			getline(file, line);
		file.close();
	}
	return line.c_str();
}
