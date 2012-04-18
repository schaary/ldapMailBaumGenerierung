
// import org.apache.directory.groovyldap.*
import groovy.sql.Sql
import oracle.jdbc.driver.*

def passwordDir = System.getProperty("user.home") + "/.password/"

def oracleCredentialFile = new File(passwordDir+"oracle_delphi1.xml")

assert oracleCredentialFile.exists(), "Konnte die Oracle Zugangsdaten nicht finden"

def oracleLogin = new XmlSlurper().parse(oracleCredentialFile)

def db = Sql.newInstance('jdbc:oracle:thin:@'+oracleLogin.host.toString()+':'+oracleLogin.port.toString()+':'+oracleLogin.instance.toString(),
                     oracleLogin.user.toString(),
                     oracleLogin.password.toString(),
                     oracleLogin.jdbc.toString())



db("BEGIN ? := mail_pkg.getUmtMailAddresses(?); END;",
[OracleTypes.INTEGER, Sql.resultSet(OracleTypes.CURSOR)]) { cursorResults ->
    while (cursorResults.next()) {
        println cursorResults.getAt(1);
    }    
} 
//if (firstResultset.size() > 600 && secondResultSet.size() > 600) {
//    db.execute("truncate table med_uk_email_tbl")
//    firstResultset.each { db.execute("insert into med_uk_email_tbl (sname, gname, mail) values (${it.sn}, ${it.givenname}, ${it.mail})") }
//    secondResultSet.each { db.execute("insert into med_uk_email_tbl (sname, gname, mail) values (${it.sn}, ${it.givenname}, ${it.mail})") }
//} else {
//    println "Das AD hat nicht genug Eintraege!"
//}


db.close()
