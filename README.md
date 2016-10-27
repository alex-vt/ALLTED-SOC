#ALLTED-SOC

The software system for organizing service-oriented computing (SOC) based on the original ALLTED package.

It provides REST API for computing tasks using ALLTED.


##Usage

The REST API available after installation can be accessed by various apps or from a REST client.


##Installation

These are the instructions how to provide ALLTED SOC from your server.

32-bit openSUSE (SUSE Linux) or Debian are used in this manual as the OS.

The first stage is optional. It is creating an instance at the cloud (IaaS) service.

The setup process for Google Cloud and Amazon EC2 is described. You can choose one of them, or use your own server.


###Set up an instance on Google Cloud

This stage is optional.

Sign up for Google Compute Engine if you haven't.

Create an instance of openSUSE with HTTP enabled.

Login to your Linux (for example, user@local in this tutorial).

Set up keys for ssh:

    user@local:~$ ssh-keygen
    Generating public/private rsa key pair.
    Enter file in which to save the key (/home/user/.ssh/id_rsa): /home/user/ssh-key
    ...

Get the public key:

    user@local:~$ cat ssh-key.pub
    ssh-rsa AAAAB3NzaC1yc...AI4faZ user@local

Copy the public key to `https://console.developers.google.com` -> Compute -> Compute Engine -> Metadata -> SSH KEYS

Login to the instance (lookup its IP
in https://console.developers.google.com -> Compute -> Compute Engine -> VM instances; for example, 127.0.0.1):

    user@local:~$ ssh -i ssh-key -o UserKnownHostsFile=/dev/null -o CheckHostIP=no -o StrictHostKeyChecking=no user@127.0.0.1


###Set up an instance on Amazon EC2

This stage is optional.

Sign up for Amazon Web Services if you haven't.

Create an instance of SUSE Linux. Save the provided .pem key file (user.pem in this manual).
Open the TCP port 80 in inbound rules.

Login to your Linux (user@local in this tutorial). Get user.pem key file there.

Use this command:

    user@local:~$ chmod 400 user.pem

Login to the openSUSE (SUSE Linux) instance as ec2-user (lookup its public IP
in `https://console.aws.amazon.com/ec2/v2/home` -> Running Instances -> Connect; for example, 127.0.0.1):

    user@local:~$ ssh -i user.pem ec2-user@127.0.0.1

or to the Debian instance as admin:

    user@local:~$ ssh -i user.pem admin@127.0.0.1


###Get the original ALLTED package working

A clean 32-bit openSUSE (SUSE Linux) or Debian system is supposed to be installed and accessible at this point.
Login into it. For example, you are logged in as user@server.

( *Deprecated, skip* ) The original ALLTED package is 32-bit. If you are running a 64-bit OS, install 32-bit libs:

    user@server:~> sudo zypper in glibc-32bit libncurses5-32bit zlib-devel-32bit libstdc++47-32bit libzzip-0-13-32bit

If you are running Debian, install unzip:

    user@server:~> sudo apt-get update
    user@server:~> sudo apt-get install unzip

Get the executable ALLTED binaries and check how it's working:

    user@server:~> cd ~
    user@server:~> wget --no-check-certificate https://www.dropbox.com/s/bxwe4wk95bpm2do/allted.zip?dl=1
    user@server:~> mv allted.zip?dl=1 allted.zip
    user@server:~> mkdir allted
    user@server:~> unzip allted.zip -d allted
    user@server:~> sudo mv allted /usr/local/share/
    user@server:~> cd /usr/local/share/allted/bin
    user@server:/usr/local/share/allted/bin> sudo chmod a+x allted ../createrpn run-task.sh
    user@server:/usr/local/share/allted/bin> sudo ./allted test.atd test.ato
    user@server:/usr/local/share/allted/bin> cat test.ato
    ***...
    A  L  L  T  E  D         A l l - T e c h n o l o g y    S y s t e m    D e s i g n e r...


###Get the Tomcat web app working

Install Java, Maven and Git on openSUSE (SUSE Linux):

    user@server:~> sudo zypper in java-1_7_0-openjdk java-1_7_0-openjdk-devel maven git

or on Debian:

    user@server:~> sudo apt-get install openjdk-7-jre openjdk-7-jdk maven git

Setup JAVA_HOME:

    user@server:~> export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-i386/jre/

Install Tomcat:

    user@server:~> cd ~
    user@server:~> wget http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.61/bin/apache-tomcat-7.0.61.tar.gz
    user@server:~> tar zxvf apache-tomcat-7.0.61.tar.gz
    user@server:~> sudo mv apache-tomcat-7.0.61 /usr/share/tomcat
    user@server:~> sudo chmod a+x /usr/share/tomcat/bin/catalina.sh

Change port from 8080 to 80 here:

    user@server:~> sudo vi /usr/share/tomcat/conf/server.xml

(Press i to insert or x to delete characters at cursor, quit editing by pressing Esc,
then save the file by typing :wq and pressing Enter.)

Start Tomcat:

    user@server:~> sudo /usr/share/tomcat/bin/catalina.sh start

Clone the web app repo. Build the web app and put it as a default (root) one into the Tomcat web apps folder:

    user@server:~> cd ~
    user@server:~> git clone https://github.com/oleksiykovtun/ALLTED-SOC.git
    user@server:~> cd ALLTED-SOC
    user@server:~> mvn package
    user@server:~> sudo mv target/allted-soc-1.0.0-SNAPSHOT.war /usr/share/tomcat/webapps/ROOT.war

View the main page `http://127.0.0.1/` (with the actual IP address of your server) in the browser.


##Creating new services

Create the class for you service using this template (for the service Example):

	package com.oleksiykovtun.allted.soc.services;

	import com.oleksiykovtun.allted.soc.base.*;

	import javax.ws.rs.Path;

	@Path("/example/")
	public class ExampleService extends Service {

		public static String DESCRIPTION = "Example service description, usage notes...";

		class ExampleTask extends Task {

			// structured task fields, setters, getters

			@Override
			public String generateAlltedTask() {
				// generate from the task fields and return the ALLTED .atd task
			}
		}

		class ExampleResult extends Result {

			// structured result fields, setters, getters

			@Override
			public void parseAlltedResult(String raw, String... plotRawData) {
				// fill the fields by parsing the ALLTED .ato result and .out plot data (you can use ResultParser)
			}
		}
	}

Then build and redeploy the web app.

New services are discovered automatically.
