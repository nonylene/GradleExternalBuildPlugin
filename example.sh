#!/usr/bin/env bash

cd `dirname $0`

## set remote server ip address or alias name
server=""

set -e

while getopts a?t:m:p: OPT; do
    case $OPT in
        a) arguments=$OPTARG
            ;;
        t) tasks=$OPTARG
            ;;
        m) module_dir=$OPTARG
            ;;
        p) project_dir=$OPTARG
            ;;
    esac
done

# unescape
gradle_args=`echo $arguments | perl -pe "s/(?<!\\\\\\),/ /g; s/\,/,/g"`
gradle_tasks=`echo $tasks | perl -pe "s/(?<!\\\\\\),//g; s/\,/,/g"`

echo "Tasks: $gradle_tasks"
echo "Args: $gradle_args"
echo "Project Dir: $project_dir"
echo "Module Dir: $module_dir"

project_relative=`realpath --relative-to=$HOME $project_dir`
module_relative=`realpath --relative-to=$HOME $module_dir`

ctl_path="$HOME/.ssh/ctl/%L-%r@%h:%p"
rsync_ssh="ssh -S $ctl_path"
mkdir -p ~/.ssh/ctl/

# create session with Control Master
echo "Opening session..."
ssh -fNM -S "$ctl_path" "$server"

# sync source files
echo "Syncing source files..."
ssh -S "$ctl_path" "$server" mkdir -p "~/$project_relative"
rsync -Cavz --delete --filter=":- .gitignore" -e "$rsync_ssh" $project_dir/ "$server":$project_relative/
# also sync build directory
mkdir -p $project_dir/build/ $module_dir/build/
rsync -CavzS --delete -e "$rsync_ssh" $project_dir/build/ "$server":$project_relative/build/
rsync -CavzS --delete -e "$rsync_ssh" $module_dir/build/ "$server":$module_relative/build/

echo "Starting Build..."
ssh -S $ctl_path "$server" "
export ANDROID_HOME=~/android-sdk-linux
cd ~/$project_relative
./gradlew $gradle_tasks $gradle_args"

echo "Syncing output files..."
rsync -CavzS --delete -e "$rsync_ssh" --exclude='*unaligned.apk' "$server":$project_relative/build/ $project_dir/build/
rsync -CavzS --delete -e "$rsync_ssh" --exclude='*unaligned.apk' "$server":$module_relative/build/ $module_dir/build/

# close session with Control Master
echo "Closing session..."
ssh -O exit -S "$ctl_path" "$server"

echo "Remote build and sync finished."

exit 0

