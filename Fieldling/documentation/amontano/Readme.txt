ABOUT THE FILES ON THIS FOLDER

"QuillDriver.nsi" contains the NSIS script to produce an installer for QuillDriverTibetan. All files needed would be automatically found by the relative path names except for two additional files which should be downloaded separatedly. These are the installers for Quicktime and JRE. Simply modify PATH_TO_INSTALLERS, QUICKTIME_INSTALLER, and JRE_INSTALLER to point to the correct location on your computer. The installer includes Tibetan Machine Web which are assumed to be in "c:\windows\fonts". Use NSIS (http://nsis.sourceforge.net/) to generate the installer.

The file "InstallationMessages.xls" contains the messages in English and Chinese used in the installer. Here the Chinese messages are in Unicode. Nevertheless NSIS does not support Unicode, so in order to convert the messages into Chinese non-unicode paste them into BabelPad (http://www.babelstone.co.uk/Software/BabelPad.html) and select "File" -> "Export..." and choose "GB18030" as encoding and save it as "InstallationMessages.txt". Then copy and paste the individual messages into their respective places in "QuillDriver.nsi". Remember to to a return after "$\n\".

System requirements:
- A Pentium processor-based PC or compatible computer
- At least 128MB of RAM
- Win 98/Me/2000/XP

NSIS uses MBCS and does not support Unicode languages, because Windows 9x/ME don't support Unicode. If you choose Chinese for the installer language, in order for the script to display correctly check that you have installed the Windows support for that language, and check that the specified language is the default language for non-Unicode programs. You can check or set it in "Control Panel" -> "Regional and Language Options". Under the "Languages" tab and make sure "Install files for East Asian languages" is enabled. Under the "Advanced" tab, make sure that "Chinese (PRC)" is selected as the language for non-Unicode programs.