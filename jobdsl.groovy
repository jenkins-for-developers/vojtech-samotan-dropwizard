
def generuj_dobry_job_pro_atym(String path, String suffix, Map m, String shellCmd)
{
    freeStyleJob("${path}/${m.name}") {
        logRotator(365, 500, 365, 10)

        concurrentBuild(true)

        jdk('OpenJDK 8')

        scm {
            git {
                remote {
                    github("jenkins-for-developers/$JMENO-$PRIJMENI-${suffix}", "https")
                }
                branch("origin/$m.branch")
            }
        }

        triggers {
            githubPush()
            scm("@daily") {
            }
        }


        wrappers {
            colorizeOutput()
            timestamps()
        }

        steps {
            maven {
                mavenInstallation('Maven 3.3.9')
                goals('clean verify -Dmaven.test.failure.ignore=true')
                goals('checkstyle:checkstyle')
            }
            shell("""\
                set +x\n
                echo "\\n\\n\\n"\n
                ${shellCmd}\n
                echo "\\n\\n\\n"
            """.stripIndent())
        }

        publishers {
            if (m.artifacts) {
                archiveArtifacts {
                    pattern('target/*.jar')
                    onlyIfSuccessful()
                    fingerprint()
                }
            }
            fingerprint('pom.xml')
            recordIssues {
                tools {
                    mavenConsole()
                    java()
                    javaDoc()
                    checkStyle()
                }
            }
            archiveJunit('target/*-reports/*.xml') {
                allowEmptyResults()
                retainLongStdout()
                testDataPublishers {
                    publishTestStabilityData()
                }
            }
            jacocoCodeCoverage {
                sourcePattern('src/**')
            }
        }
    }
}


def vytvor_adresare(String cesta) {
    dirs = cesta.split('/')
    for (int i = 0; i < dirs.size(); i++) {
        folder(dirs[0..i].join('/'))
    }
}


all_jobs = [
        [name: 'bugfix',  branch: 'bugfix/**',  artifacts: false],
        [name: 'feature', branch: 'feature/**', artifacts: false],
        [name: 'develop', branch: 'develop',    artifacts: true],
        [name: 'master',  branch: 'master',     artifacts: true],
]



def adresar = 'TEAMS/A-TEAM/01-HELLO-FROM-CLI'
def shellCmd = 'java -jar target/*.jar'

vytvor_adresare(adresar)
for (job_info in all_jobs) {
    generuj_dobry_job_pro_atym(adresar, "java", job_info, shellCmd)
}



adresar = 'TEAMS/A-TEAM/02-HELLO-FROM-WEB'
shellCmd = 'java -jar target/demoapp.jar --version'

vytvor_adresare(adresar)
for (job_info in all_jobs) {
    generuj_dobry_job_pro_atym(adresar, "dropwizard", job_info, shellCmd)
}
