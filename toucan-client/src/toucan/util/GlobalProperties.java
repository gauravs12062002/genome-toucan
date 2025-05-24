
/*This file is part of TOUCAN
 *
 * Copyright (C) 2001,2002,2003,2004,2005 Stein Aerts, University of Leuven, Belgium.
 * Contact: <stein.aerts@esat.kuleuven.ac.be>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package toucan.util;
import toucan.EnsemblSpecies;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.File;
import java.lang.ClassLoader;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Title: GlobalProperties
 * Description: Makes properties of the basics.properties file available to other classes
 * Copyright:    Copyright (c) 2004, 2005
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@med.kuleuven.be> & Peter Van Loo <Peter.VanLoo@med.kuleuven.be>
 * @version 2.0
 */

public class GlobalProperties
    implements Serializable {

  public GlobalProperties() {
    Properties props = new Properties();
    InputStream in = null;
    readProperties();
  }

  private static String projectDir;
  private static String currentDir;
  private static String[] soapServers;
  private static String soapBackup;
  private static int nrSoapServers;
  private static boolean debugYesNo = false;

  private static String ensemblMart;
  private static String vegaMart;
  private static String ensemblRelease;
  private static String ensemblMysql;
  private static String ensemblMysqlPort;
  private static String martMysql;
  private static String ensemblUser;
  private static String ensemblPass;
  private static String ensemblPath;
  private static String ensemblDriver;

  private static String jdbcDriver;

  private static boolean isUpdated = false;
  private static Properties basicProps,userProps;
  private static EnsemblSpecies[] speciesArr;

  private static String ensemblPhylogeneticTree;

  public static void readProperties() {
    ClassLoader cl = ResourceAnchor.class.getClassLoader();
    InputStream in = null;
    Properties userProps = null;
    basicProps = new Properties();
    userProps = new Properties();
    try {

         System.out.println("User.dir= "+ System.getProperty("user.dir"));
         File propFile = new File(System.getProperty("user.dir")+"/user.properties");
         if (propFile.exists()) {
          in = new FileInputStream(propFile);
          System.out.println("User Properties found in installation directory: "+propFile.getAbsolutePath());
          userProps.load(in);
         }
         in = cl.getResourceAsStream("properties/basics.properties");
         System.out.println("in="+in);
         if(in==null) in = new FileInputStream("properties/basics.properties");
         else System.out.println("Taking properties from jar.");
         basicProps.load(in);
         if(basicProps.size()>0) System.out.println("Basics.properties file found internally.");
         else System.out.println("Problem: no properties found!");


      //If we'd work with a basics.properties in the jar, and a user.properties in the directory
      //
      /*MainFrame.writeToStatus("User.dir= " + System.getProperty("user.dir"));
      File userPropFile = new File(System.getProperty("user.dir") +
                                   "/user.properties");
      if (userPropFile.exists()) {
        in = new FileInputStream(userPropFile);
        MainFrame.writeToStatus("User properties found in installation directory: " +
                           userPropFile.getAbsolutePath());
      }
      if (in != null) {
        userProps = new Properties();
        userProps.load(in);
      }
      in = null;
      in = cl.getResourceAsStream("properties/basics.properties");
      if (in == null) in = new FileInputStream("properties/basics.properties");
      MainFrame.writeToStatus("Basic Properties file taken internally");
      basicProps.load(in);*/

    }
    catch (IOException ioe) {
      ioe.printStackTrace(System.out);
    }

    /*MainFrame.writeToStatus("Properties are:");
         for (Enumeration e = basicProps.propertyNames(); e.hasMoreElements();) {
      String name = (String)e.nextElement();
      MainFrame.writeToStatus(name + "=" + basicProps.getProperty(name));
         }*/

    nrSoapServers = Integer.parseInt(basicProps.getProperty("NR_SOAP_SERVERS"));
    soapBackup = basicProps.getProperty("SOAP_SERVER_BACKUP");
    //System.out.println(basicProps);
    soapServers = new String[nrSoapServers];
    for (int i = 0;i<nrSoapServers;i++){
      soapServers[i] = basicProps.getProperty("SOAP_SERVER_"+i);
    }
    if (userProps.containsKey("debug") &&
        userProps.getProperty("debug").equalsIgnoreCase("on")) {
      debugYesNo = true;
    }

    ensemblMart = basicProps.getProperty("ensembl_mart");
    System.out.println(ensemblMart);
    vegaMart = basicProps.getProperty("vega_mart");
    ensemblRelease = basicProps.getProperty("ensembl_release");
    ensemblMysql = basicProps.getProperty("ensembl_mysql");
    ensemblMysqlPort = basicProps.getProperty("ensembl_mysql_port");
    martMysql = basicProps.getProperty("mart_mysql");
    ensemblUser = basicProps.getProperty("ensembl_user");
    ensemblPass = basicProps.getProperty("ensembl_passwd");
    ensemblPath = basicProps.getProperty("ensembl_path");
    ensemblDriver = basicProps.getProperty("ensembl_driver");
    jdbcDriver = basicProps.getProperty("jdbc_driver");
    ensemblPhylogeneticTree = basicProps.getProperty("ensembl_phylogenetic_tree");
    if (userProps != null) {
      projectDir = userProps.getProperty("PROJECT_DIR");
    }

    try {
      makeSpeciesArr();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    isUpdated = true;
  }

  public static void reReadFromFile() {
    isUpdated = false;
  }

  // user config setters and getters
  public static void setProjectDir(String input) {
    projectDir = input;
  }

  public static String getProjectDir() throws IOException {
    if (isUpdated) {
      return projectDir;
    }
    readProperties();
    return projectDir;
  }

  public static void setCurrentDir(String input) {
    currentDir = input;
  }

  public static String getCurrentDir() {
    if (isUpdated) {
      return currentDir;
    }
    readProperties();
    return currentDir;
  }

  public static void setDebugYesNo(boolean input) {
    debugYesNo = input;
  }

  public static boolean getDebugYesNo() throws IOException {
    if (isUpdated) {
      return debugYesNo;
    }
    readProperties();
    return debugYesNo;
  }

  //public static void setSOAPServer(String input) {
  //  soapServer = input;
  //}

  public static String[] getSOAPServers() {
    if (isUpdated) {
      return soapServers;
    }
    readProperties();
    return soapServers;
  }

  public static int getNrSOAPServers() {
    if (isUpdated) {
      return nrSoapServers;
    }
    readProperties();
    return nrSoapServers;
  }

  public static String getSOAPBackup() {
  if (isUpdated) {
    return soapBackup;
  }
  readProperties();
  return soapBackup;
}



  public static void setEnsemblMart(String input) {
    ensemblMart = input;
  }

  public static String getEnsemblMart() {
    if (isUpdated) {
      return ensemblMart;
    }
    readProperties();
    return ensemblMart;
  }

  public static void setVegaMart(String input) {
    vegaMart = input;
  }

  public static String getVegaMart() {
    if (isUpdated) {
      return vegaMart;
    }
    readProperties();
    return vegaMart;
  }

  public static void setEnsemblRelease(String input) {
    ensemblRelease = input;
  }

  public static String getEnsemblRelease() {
    if (isUpdated) {
      return ensemblRelease;
    }
    readProperties();
    return ensemblRelease;
  }

  public static void setEnsemblMysql(String input) {
    ensemblMysql = input;
  }
  public static void setMartMysql(String input) {
	    martMysql = input;
	  }

  public static String getEnsemblMysql() {
    if (isUpdated) {
      return ensemblMysql;
    }
    readProperties();
    return ensemblMysql;
  }

  public static String getEnsemblMysqlPort() {
    if (isUpdated) {
      return ensemblMysqlPort;
    }
    readProperties();
    return ensemblMysqlPort;
  }

  public static String getMartMysql() {
	    if (isUpdated) {
	      return martMysql;
	    }
	    readProperties();
	    return martMysql;
	  }


  
  public static void setEnsemblUser(String input) {
    ensemblUser = input;
  }

  public static String getEnsemblUser() {
    if (isUpdated) {
      return ensemblUser;
    }
    readProperties();
    return ensemblUser;
  }

  public static void setEnsemblPass(String input) {
    ensemblPass = input;
  }

  public static String getEnsemblPass() {
    if (isUpdated) {
      return ensemblPass;
    }
    readProperties();
    return ensemblPass;
  }

  public static void setEnsemblPath(String input) {
  ensemblPath = input;
}

public static String getEnsemblPath() {
  if (isUpdated) {
    return ensemblPath;
  }
  readProperties();
  return ensemblPath;
}
public static void setEnsemblDriver(String input) {
  ensemblDriver = input;
}

public static String getEnsemblDriver() {
  if (isUpdated) {
    return ensemblDriver;
  }
  readProperties();
  return ensemblDriver;
}
public static void setJdbcDriver(String input) {
  jdbcDriver = input;
}

public static String getJdbcDriver() {
  if (isUpdated) {
    return jdbcDriver;
  }
  readProperties();
  return jdbcDriver;
}

public static void setEnsemblPhylogeneticTree(String input) {
  ensemblPhylogeneticTree = input;
}

public static String getEnsemblPhylogeneticTree() {
  if (isUpdated) {
    return ensemblPhylogeneticTree;
  }
  readProperties();
  return ensemblPhylogeneticTree;
}

public static EnsemblSpecies getSpecies(String specName) {
  EnsemblSpecies ret = null;
  for (int i = 0; i < GlobalProperties.getSpeciesArr().length; i++) {
    if (GlobalProperties.getSpeciesArr()[i].name.equalsIgnoreCase(specName) ||
        GlobalProperties.getSpeciesArr()[i].longName.equalsIgnoreCase(
        specName) ||
        GlobalProperties.getSpeciesArr()[i].shortName.
        equalsIgnoreCase(specName))
      ret = GlobalProperties.getSpeciesArr()[i];
  }
  return ret;
}

  public static EnsemblSpecies[] getSpeciesArr() {
    if (isUpdated) {
      return speciesArr;
    }
    readProperties();
    return speciesArr;
  }

  public static void makeSpeciesArr() throws Exception {
    String elem;
    EnsemblSpecies s;
    StringTokenizer tok;
    elem = basicProps.getProperty("ensembl_nr_species");
    EnsemblSpecies[] species = new EnsemblSpecies[new Integer(elem).intValue()];
    for (int i = 0; i < species.length; i++) {
      species[i] = new EnsemblSpecies();
    }
    Enumeration en = basicProps.propertyNames();
    int id;
    while (en.hasMoreElements()) {
      elem = (String) en.nextElement();
      if (elem.startsWith("ensembl_species_")) {
        id = new Integer(elem.substring(16, elem.length())).intValue();
        tok = new StringTokenizer( (String) basicProps.getProperty(elem), "|");
        species[id].name = (String) tok.nextElement();
        species[id].longName = (String) tok.nextElement();
        species[id].shortName = (String) tok.nextElement();
        species[id].core = (String) tok.nextElement();
        //species[id].lite = (String)tok.nextElement();
        if (tok.hasMoreElements())
          species[id].prefix = (String) tok.nextElement();
        else
          species[id].prefix = "";
      }
      else if (elem.startsWith("ensembl_externals_")) {
        id = new Integer(elem.substring(18, elem.length())).intValue();
        tok = new StringTokenizer( (String) basicProps.getProperty(elem), "|");
        while (tok.hasMoreElements()) {
          species[id].externalNames.add( (String) tok.nextElement());
          if (tok.hasMoreElements()) species[id].externalIds.add(new Integer( (
              String) tok.nextElement()));
        }
      }
      else if (elem.startsWith("ensembl_homologs_")) {
        id = new Integer(elem.substring(17, elem.length())).intValue();
        tok = new StringTokenizer( (String) basicProps.getProperty(elem), "|");
        while (tok.hasMoreElements()) {
          species[id].homologs.add( (String) tok.nextElement());
        }
      }
    }
    speciesArr = species;
  }

}
