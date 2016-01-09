usage() { echo "Usage: deploy.sh -c to check; deploy.sh -d to deploy"; exit 1; }

checkUptime() { 
  ssh -p ${portno} ${uzer}@${domain} uptime
}

createUberJar() {
  lein uberjar
}

createRelease() {
  ssh -p${portno} ${uzer}@${domain} "cd ${release_dir}; mkdir ${1}"
}

cleanUpReleases() {
  if $(ssh -p${portno} ${uzer}@${domain} "cd ${release_dir}; [ \$(ls | wc -l) -gt ${keep} ];"); then
    # removing the oldest release
    ssh -p${portno} ${uzer}@${domain} "cd ${release_dir}; rm -r \$(ls -t | tail -${keep} | tail -n1)"
  fi;
}

symlinkExtra() {
  ssh -p${portno} ${uzer}@${domain} "cd ${release_dir}/${1}; for s in ${linked_files[@]}; do ln -s /var/www/photolog/shared/\$s ${release_dir}/${1}; done; cd ../../; if [ -h current-jar ]; then rm current-jar; fi; ln -s ${release_dir}/${1}/photolog*.jar ./current-jar; if [ -h photolog.conf ]; then rm photolog.conf; fi; ln -s ${release_dir}/${1}/photolog.conf ./photolog.conf;"
}

deployToServer() {
  timeztamp=$(date +%Y%m%d%H%M%S)

  createRelease $timeztamp
  cleanUpReleases
  createUberJar
  echo "scp\'ing jar to server"
  scp -P ${portno} ./target/photolog-*standalone.jar "${uzer}@${domain}:${release_dir}/${timeztamp}"
  scp -P ${portno} ./photolog.conf "${uzer}@${domain}:${release_dir}/${timeztamp}"
  symlinkExtra $timeztamp
  ssh -p${portno} ${uzer}@${domain} "sudo supervisorctl restart photolog"
}



if [ $OPTIND -eq 0 ]; then
  usage
  exit
fi

uzer=${UZER?"Please provide a UZER"}
portno=${PORTNO?"Please provide the portno"}
domain="mattscodecave.com"
release_dir="/var/www/photolog/releases"
keep=5
linked_files=(.lein-env albums logs)




while getopts "cdh" opt; do
  case $opt in
    c)
      echo "Checking uptime:"
      checkUptime;;
    d)
      echo "Deploying..."
      deployToServer
      echo "Deployed!";;
    h)
      usage
      exit;;
    *)
      usage
      exit;;
  esac
done;

shift $((OPTIND-1))
