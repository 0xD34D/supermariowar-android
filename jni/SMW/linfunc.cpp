#include <ctype.h>
#include <string.h>

#ifdef _DEBUG
#include <iostream>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

bool CopyFile(const char *src, const char *dest, bool dontOverwrite)
{
    struct stat fileinfo;
    FILE *s = NULL, *d = NULL;
    char buf[BUFSIZ];
    size_t actual_bufsiz;

    if ((dontOverwrite && stat(dest, &fileinfo) != -1) ||
        (s = fopen(src, "rb")) == NULL || (d = fopen(dest, "wb")) == NULL)
    {
        if (s != NULL)
             fclose(s);
        if (d != NULL)
             fclose(d);
        return false;
    }

    while ((actual_bufsiz = fread(buf, sizeof(char), BUFSIZ, s)) > 0 &&
        fwrite(buf, sizeof(char), actual_bufsiz, d) == actual_bufsiz);

    fclose(s);
    fclose(d);

    return (ferror(s) != 0 || ferror(d) != 0);
}
#endif

char *_strlwr(char *str)
{
    char *p = str;

    for (; *p; p++)
        *p = tolower(*p);

    return str;
}

int stricmp(const char *s1, const char *s2)
{
  char f, l;

  do 
  {
    f = ((*s1 <= 'Z') && (*s1 >= 'A')) ? *s1 + 'a' - 'A' : *s1;
    l = ((*s2 <= 'Z') && (*s2 >= 'A')) ? *s2 + 'a' - 'A' : *s2;
    s1++;
    s2++;
  } while ((f) && (f == l));

  return (int) (f - l);
}

