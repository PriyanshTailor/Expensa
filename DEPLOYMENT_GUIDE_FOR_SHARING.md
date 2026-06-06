# 🌍 How to Make Your App Work Anywhere (For Sharing)

If you want to share the APK with your friends and have it work **without your PC being turned on**, you must put your backend and database on the internet.

Don't worry, we can do this **100% for free** using two excellent services: **MongoDB Atlas** (for your database) and **Render** (for your Spring Boot backend).

I have already created a `Dockerfile` in your `backend_java` folder to make this easy.

---

### Step 1: Put your Database on the Internet (MongoDB Atlas)
Currently, your app saves data to `localhost:27017` which only exists on your computer.

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register) and create a free account.
2. Build a new cluster (select the **FREE** tier).
3. Under "Database Access", create a new database user (give it a username and password). **Remember this password!**
4. Under "Network Access", add the IP Address `0.0.0.0/0` (this allows connections from anywhere).
5. Click **Connect** on your cluster, choose **Connect your application**, and copy the connection string.
   *It will look something like this:* `mongodb+srv://<username>:<password>@cluster0.mongodb.net/expense_tracker?retryWrites=true&w=majority`

### Step 2: Upload your code to GitHub
Cloud services need a place to grab your code from.

1. Create a free account on [GitHub.com](https://github.com/).
2. Create a new Private Repository.
3. Upload your **entire project folder** to this repository.

### Step 3: Put your Backend on the Internet (Render)
1. Go to [Render.com](https://render.com/) and create a free account.
2. Click **New +** and select **Web Service**.
3. Connect your GitHub account and select the repository you just made.
4. Render will automatically detect the `Dockerfile` I created for you.
5. Scroll down to **Environment Variables** and add your MongoDB Atlas URL so the server knows where to save data:
   * **Key**: `SPRING_DATA_MONGODB_URI`
   * **Value**: *(Paste your MongoDB Atlas connection string from Step 1 here, remember to replace `<password>` with your actual password!)*
6. Click **Create Web Service**. 
7. Wait 5-10 minutes for it to build. Once it's done, Render will give you a public URL (e.g., `https://my-backend-xy12.onrender.com`).

### Step 4: Update the App & Generate the Final APK!
Now that your server is running on the internet 24/7, you just need to tell your Android App where to find it.

1. Open the Android App in Android Studio.
2. Open `LoginActivity.java`.
3. In the "Server Config" UI we built earlier, enter the new Render URL: `my-backend-xy12.onrender.com` 
   *(Alternatively, you can just change the default IP in `RetrofitClient.java` to your Render URL).*
4. Build the Final APK! (`Build -> Build Bundle(s) / APK(s) -> Build APK(s)`)

### 🎉 You are Done!
You can now send that APK to your friends. They can install it, open it on mobile data, and it will communicate perfectly with your free cloud backend, even when your computer is completely turned off!
