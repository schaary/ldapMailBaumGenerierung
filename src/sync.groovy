
// import org.apache.directory.groovyldap.*
import groovy.sql.Sql
import oracle.jdbc.driver.*
import java.security.MessageDigest
import org.apache.directory.groovyldap.*
// import org.apache.directory.groovyldap.LDAP

def passwordDir = System.getProperty("user.home") + "/.password/"

def ldapCredentialFile = new File(passwordDir+"ldap4_admin.xml")
def oracleCredentialFile = new File(passwordDir+"oracle_delphi1.xml")

assert oracleCredentialFile.exists(), "Konnte die Oracle Zugangsdaten nicht finden"
assert ldapCredentialFile.exists(), "Konnte die LDAP Zugangsdaten nicht finden"

def oracleLogin = new XmlSlurper().parse(oracleCredentialFile)
def ldapLogin = new XmlSlurper().parse(ldapCredentialFile)

def db = Sql.newInstance('jdbc:oracle:thin:@'+oracleLogin.host.toString()+':'+oracleLogin.port.toString()+':'+oracleLogin.instance.toString(),
                     oracleLogin.user.toString(),
                     oracleLogin.password.toString(),
                     oracleLogin.jdbc.toString())

def ldap = LDAP.newInstance(
    ldapLogin.uri.toString()+':'+ ldapLogin.port.toString() +
        '/' + ldapLogin.baseDN.toString(),
    ldapLogin.user.toString(),
   ldapLogin.password.toString())

def vArrMailBaum = []
def vSetCheckList = [] as Set

def vIntUmtCounter = 0;
def vIntSVACounter = 0;
def vIntUKCounter = 0;

def sha1 = MessageDigest.getInstance("SHA1")
def vStrCheckSumString = ''

def vArrLDAP = []


def vIntCounter = 0
vArrParams = new Search()
vArrParams.filter = '(uid=*)'
vArrParams.scope = 'SUB'
vArrParams.base = 'ou=mail'
results = ldap.search(vArrParams)
println results.size()

//ldap.eachEntry ('(uid=*)') { entry ->
//    if (++vIntCounter < 10) { println entry.uid.toString().trim() }
//    vArrLDAP.add(entry.uid.toString().trim())
//}

// println vArrLDAP.size()

def outFile = new File('uids.ldif')

vArrLDAP.each {
    outFile << it + "\n"
}

/*
db("BEGIN ? := mail_pkg.getUmtMailAddresses(?); END;",
[OracleTypes.INTEGER, Sql.resultSet(OracleTypes.CURSOR)]) { cursorResults ->
    while (cursorResults.next()) {
        vIntUmtCounter += 1;
        if (!vSetCheckList.contains(cursorResults.getAt(0))) { 
            vSetCheckList.add(cursorResults.getAt(0).toString().trim())
            vStrCheckSumString = cursorResults.getAt(0).toString().trim().toLowerCase() + 
                                 cursorResults.getAt(1).toString().trim().toLowerCase() +
                                 cursorResults.getAt(2).toString().trim().toLowerCase()
            sha1.update(vStrCheckSumString.getBytes())
            vArrMailBaum.add([  uid: new BigInteger(1, sha1.digest()),
                               mail: cursorResults.getAt(0).toString().trim(),
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
            vStrCheckSumString = cursorResults.getAt(0).toString().trim().toLowerCase() + 
                                 cursorResults.getAt(1).toString().trim().toLowerCase() +
                                 cursorResults.getAt(2).toString().trim().toLowerCase()
            sha1.update(vStrCheckSumString.getBytes())
            vArrMailBaum.add([  uid: new BigInteger(1, sha1.digest()),
                               mail: cursorResults.getAt(0).toString().trim(),
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
            vStrCheckSumString = cursorResults.getAt(0).toString().trim().toLowerCase() + 
                                 cursorResults.getAt(1).toString().trim().toLowerCase() +
                                 cursorResults.getAt(2).toString().trim().toLowerCase()
            sha1.update(vStrCheckSumString.getBytes())
            vArrMailBaum.add([  uid: new BigInteger(1, sha1.digest()),
                               mail: cursorResults.getAt(0).toString().trim(),
                              sname: cursorResults.getAt(1).toString().trim(),
                              gname: cursorResults.getAt(2).toString().trim()])
        }
    }    
} 


new File('ldapMailTree.ldif').withWriter { out ->
    vArrMailBaum.each {
        out.println 'dn: uid=' + it.uid.toString() + ',ou=mail2,o=mlu,c=de'
        out.println 'objectclass: top'
        out.println 'objectclass: person'
        out.println 'objectclass: inetorgperson'
        out.println 'uid: ' + it.uid.toString()
        out.println 'cn: ' + it.gname.toString().trim() + " " + it.sname.toString().trim()
        if (it.gname.toString().trim()) {out.println 'givenname: ' + it.gname.toString().trim()}
        out.println 'sn: ' + it.sname.toString().trim()
        out.println 'mail: ' + it.mail
        out.println ""
    }
}
*/
println "Anzahl der gefundenen UMT-Eintraege: ${vIntUmtCounter}"
println "Anzahl der gefundenen SVA-Eintraege: ${vIntSVACounter}"
println "Anzahl der gefundenen UK-Eintraege: ${vIntUKCounter}"

println "Gesamtanzahl: " + vArrMailBaum.size()
db.close()
