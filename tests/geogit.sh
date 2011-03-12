rm -rf foo
rm -rf bar

mkdir foo
cd foo
git init
git geo setup
touch file1
git add file1
git commit -m "Add file1"
git checkout -b not-master
cd ../

git clone foo bar
cd bar
git geo setup
echo "content" > file1
git commit -a -m "Write content to file1"
git geo push origin master:master
cd ..

cd foo
git remote add bar ../bar
git checkout master
git log --show-notes=geocommit
