/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2003-2011 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse Public License 1.0 which accompanies this     **
 **   distribution, and is available at:                                     **
 **   http://www.rssowl.org/legal/epl-v10.html                               **
 **                                                                          **
 **   A copy is found in the file epl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **                                                                          **
 **   This copyright notice MUST APPEAR in all copies of the file!           **
 **                                                                          **
 **   Contributors:                                                          **
 **     RSSOwl - initial API and implementation (bpasero@rssowl.org)         **
 **                                                                          **
 **  **********************************************************************  */

/**
 * The NSIS-Script to create the RSSOwl installer.
 * 
 * @author bpasero
 * @version 2.2.1
 */

;#####	 Variables	######
!define VER_DISPLAY "2.2.1"

;#####   Include Modern UI   ######
!include "MUI.nsh"

;#####   Installer Configuration   ######
Name "RSSOwl"
OutFile "RSSOwl Setup 2.2.1.exe"
InstallDir $PROGRAMFILES\RSSOwl
InstallDirRegKey HKCU "Software\RSSOwl" ""
AllowRootDirInstall true
BrandingText " "
SetCompressor /SOLID lzma

;#####   Variables   ######
Var STARTMENU_FOLDER
Var MUI_TEMP

;#####   Functions   ######
Function "ExecRSSOwl"
  SetOutPath $INSTDIR
  Exec "$INSTDIR\rssowl.exe"
FunctionEnd

;#####	Version Information	######
VIProductVersion "${VER_DISPLAY}.0"
VIAddVersionKey "ProductName" "RSSOwl"
VIAddVersionKey "CompanyName" "RSSOwl Team"
VIAddVersionKey "LegalCopyright" "Benjamin Pasero"
VIAddVersionKey "FileDescription" "RSSOwl"
VIAddVersionKey "FileVersion" "${VER_DISPLAY}"

;#####   Interface Settings   ######
!define MUI_ABORTWARNING
!define MUI_UNABORTWARNING
!define MUI_ICON "res\win-install.ico"
!define MUI_UNICON "res\win-uninstall.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP "res\header.bmp"
!define MUI_WELCOMEFINISHPAGE_BITMAP "res\welcome.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "res\welcome.bmp"

;### Remember language ###
!define MUI_LANGDLL_REGISTRY_ROOT "HKCU"
!define MUI_LANGDLL_REGISTRY_KEY "Software\RSSOwl"
!define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"


;#####   Pages   ######

;### Welcome ###
!insertmacro MUI_PAGE_WELCOME

;### License ###
!insertmacro MUI_PAGE_LICENSE "res\epl.rtf"

;### Directory ###
!insertmacro MUI_PAGE_DIRECTORY

;### Startmenu ###
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\RSSOwl"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER

;### Install Status ###
!insertmacro MUI_PAGE_INSTFILES

;### Finish ###
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION ExecRSSOwl
#!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\doc\tutorial\en\index.html"
!insertmacro MUI_PAGE_FINISH

;### Uninstall ###
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

;#####   Languages   ######
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "French"
!insertmacro MUI_LANGUAGE "Spanish"
!insertmacro MUI_LANGUAGE "Italian"
!insertmacro MUI_LANGUAGE "Dutch"
!insertmacro MUI_LANGUAGE "Danish"
!insertmacro MUI_LANGUAGE "Greek"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "PortugueseBR"
!insertmacro MUI_LANGUAGE "Norwegian"
!insertmacro MUI_LANGUAGE "Ukrainian"
!insertmacro MUI_LANGUAGE "Japanese"
!insertmacro MUI_LANGUAGE "SimpChinese"
!insertmacro MUI_LANGUAGE "Finnish"
!insertmacro MUI_LANGUAGE "Swedish"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro MUI_LANGUAGE "Polish"
!insertmacro MUI_LANGUAGE "TradChinese"
!insertmacro MUI_LANGUAGE "Hungarian"
!insertmacro MUI_LANGUAGE "Bulgarian"
!insertmacro MUI_LANGUAGE "Czech"
!insertmacro MUI_LANGUAGE "Slovenian"
!insertmacro MUI_LANGUAGE "Turkish"
!insertmacro MUI_LANGUAGE "Thai"
!insertmacro MUI_LANGUAGE "Serbian"
!insertmacro MUI_LANGUAGE "SerbianLatin"
!insertmacro MUI_LANGUAGE "Croatian"
!insertmacro MUI_LANGUAGE "Slovak"

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Function un.onInit
  !insertmacro MUI_UNGETLANGUAGE
FunctionEnd

;#####   Installer Section   ######
Section ""
  SetOutPath $INSTDIR

  ;### Delete Files if existing ###
  RMDir /r "$INSTDIR\configuration"
  RMDir /r "$INSTDIR\features"
  RMDir /r "$INSTDIR\plugins"

  ;### Copy / Create Files ###
  File /r bin\*.*
  
  ;### Unpack200 ###
  DetailPrint "$(^Extract) com.ibm.icu.base_3.8.1.v20080530.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\com.ibm.icu.base_3.8.1.v20080530.jar.pack plugins\com.ibm.icu.base_3.8.1.v20080530.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.commands_3.4.0.I20080509-2000.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.commands_3.4.0.I20080509-2000.jar.pack plugins\org.eclipse.core.commands_3.4.0.I20080509-2000.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.contenttype_3.3.0.v20080604-1400.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.contenttype_3.3.0.v20080604-1400.jar.pack plugins\org.eclipse.core.contenttype_3.3.0.v20080604-1400.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.databinding.beans_1.1.1.M20080827-0800a.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.databinding.beans_1.1.1.M20080827-0800a.jar.pack plugins\org.eclipse.core.databinding.beans_1.1.1.M20080827-0800a.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.databinding_1.1.1.M20080827-0800b.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.databinding_1.1.1.M20080827-0800b.jar.pack plugins\org.eclipse.core.databinding_1.1.1.M20080827-0800b.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.expressions_3.4.1.r342_v20081203-0800.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.expressions_3.4.1.r342_v20081203-0800.jar.pack plugins\org.eclipse.core.expressions_3.4.1.r342_v20081203-0800.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.jobs_3.4.1.R34x_v20081128.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.jobs_3.4.1.R34x_v20081128.jar.pack plugins\org.eclipse.core.jobs_3.4.1.R34x_v20081128.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.net.win32.x86_1.0.0.I20080521.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.net.win32.x86_1.0.0.I20080521.jar.pack plugins\org.eclipse.core.net.win32.x86_1.0.0.I20080521.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.net_1.1.0.I20080604.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.net_1.1.0.I20080604.jar.pack plugins\org.eclipse.core.net_1.1.0.I20080604.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.runtime.compatibility.auth_3.2.100.v20070502.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.runtime.compatibility.auth_3.2.100.v20070502.jar.pack plugins\org.eclipse.core.runtime.compatibility.auth_3.2.100.v20070502.jar'
  
  DetailPrint "$(^Extract) org.eclipse.core.runtime_3.4.0.v20080512.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.core.runtime_3.4.0.v20080512.jar.pack plugins\org.eclipse.core.runtime_3.4.0.v20080512.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.app_1.1.0.v20080421-2006.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.app_1.1.0.v20080421-2006.jar.pack plugins\org.eclipse.equinox.app_1.1.0.v20080421-2006.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.common_3.4.0.v20080421-2006.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.common_3.4.0.v20080421-2006.jar.pack plugins\org.eclipse.equinox.common_3.4.0.v20080421-2006.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.launcher_1.0.101.R34x_v20081125.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.launcher_1.0.101.R34x_v20081125.jar.pack plugins\org.eclipse.equinox.launcher_1.0.101.R34x_v20081125.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.preferences_3.2.201.R34x_v20080709.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.preferences_3.2.201.R34x_v20080709.jar.pack plugins\org.eclipse.equinox.preferences_3.2.201.R34x_v20080709.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.registry_3.4.0.v20080516-0950.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.registry_3.4.0.v20080516-0950.jar.pack plugins\org.eclipse.equinox.registry_3.4.0.v20080516-0950.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.security.win32.x86_1.0.0.v20080529-1600.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.security.win32.x86_1.0.0.v20080529-1600.jar.pack plugins\org.eclipse.equinox.security.win32.x86_1.0.0.v20080529-1600.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.security_1.0.1.R34x_v20080721.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.security_1.0.1.R34x_v20080721.jar.pack plugins\org.eclipse.equinox.security_1.0.1.R34x_v20080721.jar'
  
  DetailPrint "$(^Extract) org.eclipse.equinox.simpleconfigurator_1.0.0.v20080604.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.equinox.simpleconfigurator_1.0.0.v20080604.jar.pack plugins\org.eclipse.equinox.simpleconfigurator_1.0.0.v20080604.jar'
  
  DetailPrint "$(^Extract) org.eclipse.help_3.3.102.v20081014_34x.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.help_3.3.102.v20081014_34x.jar.pack plugins\org.eclipse.help_3.3.102.v20081014_34x.jar'
  
  DetailPrint "$(^Extract) org.eclipse.jface.databinding_1.2.1.M20080827-0800a.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.jface.databinding_1.2.1.M20080827-0800a.jar.pack plugins\org.eclipse.jface.databinding_1.2.1.M20080827-0800a.jar'
  
  DetailPrint "$(^Extract) org.eclipse.jface_3.4.2.M20090107-0800.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.jface_3.4.2.M20090107-0800.jar.pack plugins\org.eclipse.jface_3.4.2.M20090107-0800.jar'
  
  DetailPrint "$(^Extract) org.eclipse.osgi_3.4.3.R34x_v20081215-1030.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.osgi_3.4.3.R34x_v20081215-1030.jar.pack plugins\org.eclipse.osgi_3.4.3.R34x_v20081215-1030.jar'
  
  DetailPrint "$(^Extract) org.eclipse.rcp_3.4.1.R342_v20090205.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.rcp_3.4.1.R342_v20090205.jar.pack plugins\org.eclipse.rcp_3.4.1.R342_v20090205.jar'
  
  DetailPrint "$(^Extract) org.eclipse.swt.win32.win32.x86_3.4.1.v3452b.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.swt.win32.win32.x86_3.4.1.v3452b.jar.pack plugins\org.eclipse.swt.win32.win32.x86_3.4.1.v3452b.jar'
  
  DetailPrint "$(^Extract) org.eclipse.swt_3.4.2.v3452b.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.swt_3.4.2.v3452b.jar.pack plugins\org.eclipse.swt_3.4.2.v3452b.jar'
  
  DetailPrint "$(^Extract) org.eclipse.ui.forms_3.3.103.v20081027_34x.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.ui.forms_3.3.103.v20081027_34x.jar.pack plugins\org.eclipse.ui.forms_3.3.103.v20081027_34x.jar'
  
  DetailPrint "$(^Extract) org.eclipse.ui.net_1.0.0.I20080605.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.ui.net_1.0.0.I20080605.jar.pack plugins\org.eclipse.ui.net_1.0.0.I20080605.jar'
  
  DetailPrint "$(^Extract) org.eclipse.ui.workbench_3.4.2.M20090127-1700.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.ui.workbench_3.4.2.M20090127-1700.jar.pack plugins\org.eclipse.ui.workbench_3.4.2.M20090127-1700.jar'
  
  DetailPrint "$(^Extract) org.eclipse.ui_3.4.2.M20090204-0800.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.ui_3.4.2.M20090204-0800.jar.pack plugins\org.eclipse.ui_3.4.2.M20090204-0800.jar'

  DetailPrint "$(^Extract) org.eclipse.update.configurator_3.2.201.R34x_v20080819.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.update.configurator_3.2.201.R34x_v20080819.jar.pack plugins\org.eclipse.update.configurator_3.2.201.R34x_v20080819.jar'
  
  DetailPrint "$(^Extract) org.eclipse.update.core.win32_3.2.100.v20080107.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.update.core.win32_3.2.100.v20080107.jar.pack plugins\org.eclipse.update.core.win32_3.2.100.v20080107.jar'
  
  DetailPrint "$(^Extract) org.eclipse.update.core_3.2.202.R34x_v20081128.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.update.core_3.2.202.R34x_v20081128.jar.pack plugins\org.eclipse.update.core_3.2.202.R34x_v20081128.jar'
  
  DetailPrint "$(^Extract) org.eclipse.update.ui_3.2.101.R34x_v20081128.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.eclipse.update.ui_3.2.101.R34x_v20081128.jar.pack plugins\org.eclipse.update.ui_3.2.101.R34x_v20081128.jar'
  
  DetailPrint "$(^Extract) org.rssowl.core_2.2.1.201312301314.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.rssowl.core_2.2.1.201312301314.jar.pack plugins\org.rssowl.core_2.2.1.201312301314.jar'
  
  DetailPrint "$(^Extract) org.rssowl.lib.db4o_2.2.1.201312301314.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.rssowl.lib.db4o_2.2.1.201312301314.jar.pack plugins\org.rssowl.lib.db4o_2.2.1.201312301314.jar'
  
  DetailPrint "$(^Extract) org.rssowl.lib.httpclient_2.2.1.201312301314.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.rssowl.lib.httpclient_2.2.1.201312301314.jar.pack plugins\org.rssowl.lib.httpclient_2.2.1.201312301314.jar'
  
  DetailPrint "$(^Extract) org.rssowl.lib.jdom_2.2.1.201312301314.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.rssowl.lib.jdom_2.2.1.201312301314.jar.pack plugins\org.rssowl.lib.jdom_2.2.1.201312301314.jar'
  
  DetailPrint "$(^Extract) org.rssowl.lib.lucene_2.2.1.201312301314.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.rssowl.lib.lucene_2.2.1.201312301314.jar.pack plugins\org.rssowl.lib.lucene_2.2.1.201312301314.jar'
  
  DetailPrint "$(^Extract) org.rssowl.ui_2.2.1.201312301314.jar"
  nsExec::ExecToStack '"plugins\unpack200.exe" -r plugins\org.rssowl.ui_2.2.1.201312301314.jar.pack plugins\org.rssowl.ui_2.2.1.201312301314.jar'
  
  Delete "$INSTDIR\plugins\unpack200.exe"
  
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  ;### Startmenu ###
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\RSSOwl.lnk" "$INSTDIR\rssowl.exe" "" "$INSTDIR\rssowl.ico"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
    CreateShortcut "$QUICKLAUNCH\RSSOwl.lnk" "$INSTDIR\rssowl.exe" "" "$INSTDIR\rssowl.ico"
    CreateShortCut "$DESKTOP\RSSOwl.lnk" "$INSTDIR\rssowl.exe" "" "$INSTDIR\rssowl.ico"
  !insertmacro MUI_STARTMENU_WRITE_END
  
  WriteINIStr "$SMPROGRAMS\$STARTMENU_FOLDER\Visit Homepage.url" "InternetShortcut" "URL" "http://www.rssowl.org"
  
  WriteRegStr HKCU "Software\RSSOwl" "" $INSTDIR
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RSSOwl" "DisplayName" "RSSOwl"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RSSOwl" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RSSOwl" "DisplayIcon" "$INSTDIR\rssowl.ico"

  ;### Register to feed Protocol ###
  WriteRegStr HKCR "feed" "" "URL:feed Protocol"
  WriteRegStr HKCR "feed" "URL Protocol" ""
  WriteRegStr HKCR "feed\DefaultIcon" "" "$\"$INSTDIR\rssowl.exe$\""
  WriteRegStr HKCR "feed\shell\open\command" "" "$\"$INSTDIR\rssowl.exe$\" $\"%1$\""
SectionEnd


;#####   Uninstaller Section   ######
Section "Uninstall"

  ;### Uninstall Files ###
  Delete "$INSTDIR\.eclipseproduct"
  Delete "$INSTDIR\RSSOwl.exe"
  Delete "$INSTDIR\rssowl.ico"
  Delete "$INSTDIR\RSSOwl.ini"
  Delete "$INSTDIR\Uninstall.exe"

  RMDir /r "$INSTDIR\configuration"
  RMDir /r "$INSTDIR\features"
  RMDir /r "$INSTDIR\plugins"
  RMDir "$INSTDIR"

  ;### Uninstall Startmenu ###
  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
  
  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Visit Homepage.url"
  Delete "$SMPROGRAMS\$MUI_TEMP\RSSOwl.lnk"
  Delete "$DESKTOP\RSSOwl.lnk"
  Delete "$QUICKLAUNCH\RSSOwl.lnk"

  ;### Delete empty start menu parent diretories ###
  StrCpy $MUI_TEMP "$SMPROGRAMS\$MUI_TEMP"

  startMenuDeleteLoop:
  RMDir $MUI_TEMP
  GetFullPathName $MUI_TEMP "$MUI_TEMP\.."

  IfErrors startMenuDeleteLoopDone

  StrCmp $MUI_TEMP $SMPROGRAMS startMenuDeleteLoopDone startMenuDeleteLoop
  startMenuDeleteLoopDone:

  DeleteRegKey HKCU "Software\RSSOwl"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\RSSOwl"
  DeleteRegKey HKCR "feed"

SectionEnd