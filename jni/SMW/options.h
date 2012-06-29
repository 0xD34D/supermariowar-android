#ifndef SMW_OPTIONS_HEADER
#define SMW_OPTIONS_HEADER
#include <string>
#include <fstream>

/* To use this class, inherit it, add members representing your options, and be
 * sure to implement Trasnfer() and Defaults(). You can optionally implement
 * Check();
 
  * It would probably be more effective to implement this functionality as a
  * metaprogram, however I'm not about to write that in C or C++, and I'm not
  * about to add Python as a building dependency, unfortunately */
class Options
{
    public:
        Options(const std::string& filename);
        ~Options();

        /* Implement this routine to transfer data between your options file and
         * memory. It should basically consist of a series of calls to Do8(),
         * Do16(), Do32(), and Wread() */
        virtual bool Transfer()=0;
        virtual void Defaults()=0;
        virtual bool Check(){};

        //bool Ropen(const std::string& filename);
        //bool Wopen(const std::string& filename);
        bool Ropen();
        bool Wopen();
        void Close();
        bool Opened();

    private:
        // TODO: I have 64-bit concerns
        bool Do32 (int   *n);
        bool Do16 (short *n);
        bool Do8  (char  *n);
        bool Wread(void *n, size_t m);

        bool Writing;
        bool NativeSex;

        std::fstream File;
        std::string FileName;

};

#endif

