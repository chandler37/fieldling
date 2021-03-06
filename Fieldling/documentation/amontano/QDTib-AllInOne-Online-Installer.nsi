; Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "QuillDriver Tibetan"
!define PRODUCT_VERSION "Version 19-Apr-2007"
!define PRODUCT_PUBLISHER "THDL, University of Virginia"
!define PRODUCT_WEB_SITE "www.thdl.org"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define PRODUCT_STARTMENU_REGVAL "NSIS:StartMenuDir"
!define PATH_TO_INSTALLERS "..\..\internal-stuff\programs"
!define QUICKTIME_INSTALLER "QuickTimeInstaller.exe"
!define JRE_INSTALLER "jre-6u1-windows-i586-p.exe"
!define JNLP_URL "http://www.thdl.org/tools/quilldriver/QuillDriver-TIB-simple.jnlp"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"

; Language Selection Dialog Settings
!define MUI_LANGDLL_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_LANGDLL_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "NSIS:Language"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "..\..\gpl.txt"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Start menu page
var ICONS_GROUP
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "THDL Tools"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "${PRODUCT_STARTMENU_REGVAL}"
!insertmacro MUI_PAGE_STARTMENU Application $ICONS_GROUP
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "SimpChinese"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "..\..\build\QDTib-AllInOne-Online-Setup.exe"
InstallDir "$PROGRAMFILES\QuillDriver"
ShowInstDetails show
ShowUnInstDetails show
XPStyle on

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Section "MainSection" SEC01
  SetOverwrite ifnewer
  call GetJRE
  call getQTJava
  
  SetOutPath "$INSTDIR"
  File "QuillDriver.ico"
  File "netx.jar"

; Shortcuts
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application  
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section -AdditionalIcons
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  Pop $R0
  StrCpy $R0 '$R0\bin\javaw.exe'
  StrCpy $R1 '-jar "$INSTDIR\netx.jar" -jnlp "${JNLP_URL}"'
  push $R1
  push $R0

  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME}.lnk" "$R0" '$R1 -arg "-THDLReadonly"' "$INSTDIR\QuillDriver.ico"  
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\QuillDriver Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Uninstall QuillDriver.lnk" "$INSTDIR\uninst.exe"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section -FileAssociations
  ReadRegStr $1 HKCR ".qdt" ""
  StrCmp $1 "" NoBackup
  StrCmp $1 "QuillDriverTibetan.qdt" NoBackup
  WriteRegStr HKCR ".qdt" "backup_val" $1
NoBackup:
  WriteRegStr HKCR ".qdt" "" "QuillDriverTibetan.qdt"
  WriteRegStr HKCR ".qdt" "Content Type" "application/QuillDriverTibetan.qdt"
  ReadRegStr $0 HKCR "QuillDriverTibetan.qdt" ""
  StrCmp $0 "" 0 Skip
  WriteRegStr HKCR "QuillDriverTibetan.qdt" "" "QuillDriver Tibetan Script"
  WriteRegStr HKCR "QuillDriverTibetan.qdt\shell" "" "Open"
  WriteRegStr HKCR "QuillDriverTibetan.qdt\DefaultIcon" "" "$INSTDIR\QuillDriver.ico"
Skip:
  pop $R0
  pop $R1
  WriteRegStr HKCR "QuillDriverTibetan.qdt\shell\open\command" "" '"$R0" $R1 -arg "-THDLReadonly" -arg "%1"'
  WriteRegStr HKCR "QuillDriverTibetan.qdt\shell\open" "" "Open with QuillDriver Tibetan"
  WriteRegStr HKCR "QuillDriverTibetan.qdt\shell\edit" "" "Edit with QuillDriver Tibetan"
  WriteRegStr HKCR "QuillDriverTibetan.qdt\shell\edit\command" "" '"$R0" $R1 -arg "-THDLTranscription" -arg "%1"'
SectionEnd

Section -InstallFonts
  IfFileExists "$FONTS\timwn.ttf" nofont
  SetOutPath "$FONTS"
  File "C:\WINDOWS\Fonts\timwn.ttf"
  File "C:\WINDOWS\Fonts\timwn1.ttf"
  File "C:\WINDOWS\Fonts\timwn2.ttf"
  File "C:\WINDOWS\Fonts\timwn3.ttf"
  File "C:\WINDOWS\Fonts\timwn4.ttf"
  File "C:\WINDOWS\Fonts\timwn5.ttf"
  File "C:\WINDOWS\Fonts\timwn6.ttf"
  File "C:\WINDOWS\Fonts\timwn7.ttf"
  File "C:\WINDOWS\Fonts\timwn8.ttf"
  File "C:\WINDOWS\Fonts\timwn9.ttf"
  ReadRegStr $0 HKLM "SOFTWARE\Microsoft\Windows NT\CurrentVersion" "CurrentVersion"
  IfErrors 0 lbl_nt
    StrCpy $0 'SOFTWARE\Microsoft\Windows\CurrentVersion\Fonts'
    Goto regfont
  lbl_nt:  
    StrCpy $0 'SOFTWARE\Microsoft\Windows NT\CurrentVersion\Fonts'
  regfont:
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb (TrueType)" "timwn.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb1 (TrueType)" "timwn1.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb2 (TrueType)" "timwn2.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb3 (TrueType)" "timwn3.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb4 (TrueType)" "timwn4.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb5 (TrueType)" "timwn5.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb6 (TrueType)" "timwn6.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb7 (TrueType)" "timwn7.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb8 (TrueType)" "timwn8.ttf"
    WriteRegStr HKEY_LOCAL_MACHINE "$0" "TibetanMachineWeb9 (TrueType)" "timwn9.ttf"    
  nofont:
SectionEnd

Section -WarningForConnection
  StrCmp $LANGUAGE "1033" warningJNLP_eng
  
  ; warning in Chinese
  MessageBox MB_OK \
  'The installer has completed successfully! $\n$\n\
  IMPORTANT: Internet connection is needed to run ${PRODUCT_NAME} for the first time.$\n\
  This ensures that the most updated version of ${PRODUCT_NAME} will be installed on$\n\
  your computer. Afterwards, no Internet connection will be required for running$\n\
  ${PRODUCT_NAME}, although if one is detected the installation will automatically$\n\
  check for new updates and install them automatically.'
  Goto resume_after_warningJNLP
  
  warningJNLP_eng:
  MessageBox MB_OK \
  'The installer has completed successfully! $\n$\n\
  IMPORTANT: Internet connection is needed to run ${PRODUCT_NAME} for the first time.$\n\
  This ensures that the most updated version of ${PRODUCT_NAME} will be installed on$\n\
  your computer. Afterwards, no Internet connection will be required for running$\n\
  ${PRODUCT_NAME}, although if one is detected the installation will automatically$\n\
  check for new updates and install them automatically.'
    
  resume_after_warningJNLP:

SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

Function GetJRE
  ; Find JRE (Javaw.exe)
  ; 1 - in JAVA_HOME environment variable
  ; 2 - in the registry
  
  ;ClearErrors
  ;ReadEnvStr $R0 "JAVA_HOME"
  ;IfErrors 0 JreFound
  
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  IfErrors 0 +2
    Goto NoJre
  StrCmp $R1 "1.5" JreFound

  NoJre:
  ; Was not found. Install.
  StrCmp $LANGUAGE "1033" wantjre_eng
  
  ; message in chinese
  MessageBox MB_ICONEXCLAMATION|MB_YESNO \
  '在你的计算机上未安装过Java 1.5运行环境，$\n\
  没有它不能运行${PRODUCT_NAME}。$\n$\n\
  你现在要安装它吗？' \
  IDYES InstallJre
  Abort
    
  wantjre_eng:
  MessageBox MB_ICONEXCLAMATION|MB_YESNO \
  'Could not find a Java Runtime Environment 1.5 installed on $\n\
  your computer. Without it you cannot run ${PRODUCT_NAME}. $\n$\n\
  Would you like to install it now?' \
  IDYES InstallJre
  Abort
  
  InstallJre:
  StrCmp $LANGUAGE "1033" jrewarning_eng
  
  ; warning in Chinese
  MessageBox MB_OK \
  '安装程序将开始安装Java 1.5虚拟机。$\n$\n\
  提示：若询问重新启动计算机请点击NO(不)$\n\
  使之继续安装${PRODUCT_NAME}。'
  goto resume_after_jrewarning
  
  jrewarning_eng:
  MessageBox MB_OK \
  'The installer for the Java Runtime Enviroment 1.5 will begin now. $\n$\n\
  IMPORTANT: If it asks to restart the computer please click NO$\n\
  in order to complete the ${PRODUCT_NAME} installation.'
  
  resume_after_jrewarning:
  SetOutPath "$TEMP"
  File "${PATH_TO_INSTALLERS}\${JRE_INSTALLER}"
  ExecWait "$TEMP\${JRE_INSTALLER}"
  Delete "$TEMP\${JRE_INSTALLER}"
  
  ; Now find the path again
  ;ClearErrors
  ;ReadEnvStr $R0 "JAVA_HOME"
  ;IfErrors 0 JreFound
  
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  IfErrors 0 +2
    Goto AgainNoJre
  StrCmp $R1 "1.5" JreFound
  
  AgainNoJre:
  StrCmp $LANGUAGE "1033" abortjre_eng
  
  ; Abort in Chinese
  Abort '未能找到Java 1.5运行环境，安装失败。'
  
  abortjre_eng:  
  Abort 'Unable to find Java Runtime Environment 1.5. Installation failed.'
  
  JreFound:
  Push $R0
FunctionEnd

Function getQTJava
  
  ReadRegDWORD $R1 HKLM "SOFTWARE\Apple Computer, Inc.\QuickTime" "Version"
  IfErrors qtnotfound
  pop $R0
  StrCpy $R1 "$R0\lib\ext\QTJava.zip"
  IfFileExists "$R1" allSet

  ; QTJava not found
  SetOutPath "$R0\lib\ext"
  File "${PATH_TO_INSTALLERS}\QTJava.zip"
  Goto allSet

  qtnotfound:
  ; Was not found. Install.
  StrCmp $LANGUAGE "1033" wantqt_eng
  
  ; message in Chinese
  MessageBox MB_ICONEXCLAMATION|MB_YESNO \
  '在你的计算机上未安装过QuickTime for Java，$\n\
  没有它不能运行${PRODUCT_NAME}。$\n$\n\
  你现在要安装它吗？'\
  IDYES InstallQT
  Abort
  
  wantqt_eng:
  MessageBox MB_ICONEXCLAMATION|MB_YESNO \
  'Could not find QuickTime for Java installed on your computer. Without it $\n\
  you cannot run ${PRODUCT_NAME}. Would you like to install it now?'\
  IDYES InstallQT
  Abort
  
  InstallQT:
  StrCmp $LANGUAGE "1033" warningqt_eng
  
  ; warning in Chinese
  MessageBox MB_OK \
  'The installer for QuickTime will begin now.'
  Goto resume_after_qtwarning
  
  warningqt_eng:
  MessageBox MB_OK \
  'The installer for QuickTime will begin now.'
  
  resume_after_qtwarning:
  SetOutPath "$TEMP"
  File "${PATH_TO_INSTALLERS}\${QUICKTIME_INSTALLER}"
  ExecWait "$TEMP\${QUICKTIME_INSTALLER}"

  StrCmp $LANGUAGE "1033" pleasewaitafterqt_eng
  
  ; warning in Chinese
  MessageBox MB_OK \
  'After the QuickTime installer has finished completely,$\n\
  click OK to resume the installation of ${PRODUCT_NAME}.'
  Goto resume_after_qtwait
  
  pleasewaitafterqt_eng:
  MessageBox MB_OK \
  'After the QuickTime installer has finished completely,$\n\
  click OK to resume the installation of ${PRODUCT_NAME}.'
  
  resume_after_qtwait:

  Delete "$TEMP\${QUICKTIME_INSTALLER}"
  
  ReadRegDWORD $R1 HKLM "SOFTWARE\Apple Computer, Inc.\QuickTime" "Version"
  IfErrors gtjavanotfound_notfound
  StrCpy $R1 "$R0\lib\ext\QTJava.zip"
  IfFileExists "$R1" allSet
  
  gtjavanotfound_notfound:
  StrCmp $LANGUAGE "1033" abortqt_eng
  
  ;Abort in Chinese
  Abort '未能找到QuickTime for Java，安装失败。'
  
  abortqt_eng:
  Abort 'Unable to find QuickTime for Java. Installation failed.'
  
  allSet:
FunctionEnd

Function un.onUninstSuccess
  HideWindow
  StrCmp $LANGUAGE "1033" removesuccess_eng
  ; Ask in Chinese
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name)从你计算机上已成功删除。"
  Goto resume_after_remove
  
  removesuccess_eng:
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
  resume_after_remove:
FunctionEnd

Function un.onInit
!insertmacro MUI_UNGETLANGUAGE
  StrCmp $LANGUAGE "1033" askifsure_eng
  ; ask in Chinese
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "你确定从你计算机上要完全删除$(^Name) 及其组件？" IDYES issure
  Abort
  
  askifsure_eng:
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES issure
  Abort
  issure:
FunctionEnd

Section Uninstall
  !insertmacro MUI_STARTMENU_GETFOLDER "Application" $ICONS_GROUP
  Delete "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME}.lnk"
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\netx.jar"
  Delete "$INSTDIR\QuillDriver.ico"

  Delete "$SMPROGRAMS\$ICONS_GROUP\Uninstall QuillDriver.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\QuillDriver Website.lnk"

  RMDir "$SMPROGRAMS\$ICONS_GROUP"
  RMDir "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  
  ;start of restore script
  ReadRegStr $1 HKCR ".qdt" ""
  StrCmp $1 "QuillDriverTibetan.qdt" 0 NoOwn ; only do this if we own it
  ReadRegStr $1 HKCR ".qdt" "backup_val"
  StrCmp $1 "" 0 Restore ; if backup="" then delete the whole key
  DeleteRegKey HKCR ".qdt"
  Goto NoOwn
Restore:
  WriteRegStr HKCR ".qdt" "" $1
  DeleteRegValue HKCR ".qdt" "backup_val" 
  DeleteRegKey HKCR "QuillDriverTibetan.qdt" ;Delete key with association settings

NoOwn:
  SetAutoClose true
SectionEnd