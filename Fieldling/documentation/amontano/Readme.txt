ABOUT THE FILES ON THIS FOLDER

MessageBundle.xls contains all messages found on QuillDriver currently in English and Chinese (Unicode). This file is used to manually produce messageBundle.properties and messageBundle_zh.txt. Both messages are simply in the format of:
message-name = message

Do produce this files simply put the message name columns (first column) and the appropriate column (second for English, third for Chinese) together and paste it into a text file. Then replace the tab for " = " (space equal sign space, do not include the quotes). "messageBundle.properties" should be saved in ASCII format. "messageBundle_zh.txt" should be saved as UTF-8. Afterwards run the batch file "encode-chinese.bat" to convert the UTF-8 ("messageBundle_zh.txt") file into ASCII escape characters ("messageBundle_zh.properties"). Then open "messageBundle_zh.properties" and delete the first weird escape sequence ("\ufeff") that shows up before the name of the first message. I don't understand why it gets inserted automatically. Finally copy "messageBundle.properties" and "messageBundle_zh.properties"  to the "src" folder.

"QuillDriver.nsi" contains the NSIS script to produce an installer for QuillDriverTibetan. All files needed would be automatically found by the relative path names except for two additional files which should be downloaded separatedly. These are the installers for Quicktime and JRE. Simply modify PATH_TO_INSTALLERS, QUICKTIME_INSTALLER, and JRE_INSTALLER to point to the correct location on your computer. The installer includes Tibetan Machine Web which are assumed to be in "c:\windows\fonts". Use NSIS (http://nsis.sourceforge.net/) to generate the installer.

The file "InstallationMessages.xls" contains the messages in English and Chinese used in the installer. Here the Chinese messages are in Unicode. Nevertheless NSIS does not support Unicode, so in order to convert the messages into Chinese non-unicode paste them into BabelPad (http://www.babelstone.co.uk/Software/BabelPad.html) and select "File" -> "Export..." and choose "GB18030" as encoding and save it as "InstallationMessages.txt". Then copy and paste the individual messages into their respective places in "QuillDriver.nsi". Remember to to a return after "$\n\".

System requirements:
- A Pentium processor-based PC or compatible computer
- At least 128MB of RAM
- Win 98/Me/2000/XP

NSIS uses MBCS and does not support Unicode languages, because Windows 9x/ME don't support Unicode. If you choose Chinese for the installer language, in order for the script to display correctly check that you have installed the Windows support for that language, and check that the specified language is the default language for non-Unicode programs. You can check or set it in "Control Panel" -> "Regional and Language Options". Under the "Languages" tab and make sure "Install files for East Asian languages" is enabled. Under the "Advanced" tab, make sure that "Chinese (PRC)" is selected as the language for non-Unicode programs.