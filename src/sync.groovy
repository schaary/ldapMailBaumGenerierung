
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


def vArrMailBaum = []
def vSetCheckList = [] as Set

def vIntUmtCounter = 0;
def vIntSVACounter = 0;
def vIntUKCounter = 0;

db("BEGIN ? := mail_pkg.getUmtMailAddresses(?); END;",
[OracleTypes.INTEGER, Sql.resultSet(OracleTypes.CURSOR)]) { cursorResults ->
    while (cursorResults.next()) {
        vIntUmtCounter += 1;
        if (!vSetCheckList.contains(cursorResults.getAt(0))) { 
            vSetCheckList.add(cursorResults.getAt(0).toString().trim())
            vArrMailBaum.add([ mail: cursorResults.getAt(0).toString().trim(),
                              sname: cursorResults.getAt(1).toString().trim(),
                              gname: cursorResults.getAt(2).toString().trim()])
        }
    }    
} 


db("BEGIN ? := mail_pkg.getSVAMailAddresses(?); END;",
[OracleTypes.INTEGER, Sql.resultSet(OracleTypes.CURSOR)]) { cursorResults ->
    while (cursorResults.next()) {
        vIntSVACounter += 1;
        if (!vSetCheckList.contains(cursorResults.getAt(0))) { 
            vSetCheckList.add(cursorResults.getAt(0).toString().trim())
            vArrMailBaum.add([ mail: cursorResults.getAt(0).toString().trim(),
                              sname: cursorResults.getAt(1).toString().trim(),
                              gname: cursorResults.getAt(2).toString().trim()])
        }
    }    
} 

db("BEGIN ? := mail_pkg.getUKMailAddresses(?); END;",
[OracleTypes.INTEGER, Sql.resultSet(OracleTypes.CURSOR)]) { cursorResults ->
    while (cursorResults.next()) {
        vIntUKCounter += 1;
        if (cursorResults.getAt(0).toString().trim()[0] != '0' && !vSetCheckList.contains(cursorResults.getAt(0))) { 
            vSetCheckList.add(cursorResults.getAt(0).toString().trim())
            vArrMailBaum.add([ mail: cursorResults.getAt(0).toString().trim(),
                              sname: cursorResults.getAt(1).toString().trim(),
                              gname: cursorResults.getAt(2).toString().trim()])
        }
    }    
} 

//vArrMailBaum.each { if (it.sname == 'Schaarschmidt') { println it } }
def vFileLdifFile = new File('ldapMailTree.ldif')

vArrMailBaum.each {
    sname = it.sname
    vFileLdifFile.write('objectclass: top')
    vFileLdifFile.write('objectclass: person')
    vFileLdifFile.write('objectclass: inetorgperson')
    vFileLdifFile.write('cn: ' + it.gname.toString().trim() + " " + it.sname.toString().trim())
    vFileLdifFile.write('givenname: ' + it.gname.toString().trim())
    vFileLdifFile.write('sn: ' + it.sname.toString().trim())
    vFileLdifFile.write('mail: ' + it.mail)
    vFileLdifFile.write("")
}


println "Anzahl der gefundenen UMT-Eintraege: ${vIntUmtCounter}"
println "Anzahl der gefundenen SVA-Eintraege: ${vIntSVACounter}"
println "Anzahl der gefundenen UK-Eintraege: ${vIntUKCounter}"

println "Gesamtanzahl: " + vArrMailBaum.size()
db.close()
