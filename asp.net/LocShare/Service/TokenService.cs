using LocShare.Models.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;

namespace LocShare.Service
{
    public static class TokenService
    {
        static TokenService()
        {
            aes = new FzLib.Cryptography.Aes();
            aes.SetStringKey(TokenPassword);
            aes.Manager.Mode = CipherMode.ECB;
        }
        /// <summary>
        /// Token有效期
        /// </summary>
     public   static readonly TimeSpan TokenPeriod = TimeSpan.FromDays(365);
     public   static readonly string TokenPassword = "helloworld";
        private static FzLib.Cryptography.Aes aes;
        public static bool TestMode { get; set; } = false;
        public static bool IsTokenValid(DbModel db, UserEntity user)
        {
            return IsTokenValid(db, user, out string message);
        }
        public static bool IsTokenValid(DbModel db, UserEntity user,out string message)
        {
            if(TestMode)
            {
                message = "";
                return true;
            }
            try
            {
                var token = user.Token;
                if (string.IsNullOrEmpty(token))
                {
                    message = "验证信息为空";
                    return false;
                }
                string decryption = aes.Decrypt(UrlDecode(user.Token));
                long timeBin = long.Parse(decryption.Substring(0, 20));
                if(DateTime.FromBinary(timeBin).Add(TokenPeriod)<DateTime.UtcNow)
                {
                    message = "登录信息已过期";
                    return false;
                }
                if(decryption.Substring(20)!=user.Name)
                {
                    message = "用户名不匹配";
                    return false;
                }
                if(db.User.Find(user.Name)==null)
                {
                    message = "用户不存在";
                    return false;
                }
                message = "";
                return true;
            }
            catch
            {
                message = "验证出错";
                return false;
            }
        }

        public static string GetToken(UserEntity user)
        {
            string name = user.Name;
            string timeString = DateTime.UtcNow.ToBinary().ToString().PadLeft(20);
            return UrlEncode(aes.Encrypt(timeString + name));

        }

        /// <summary>
        /// AES 算法加密
        /// </summary>
        /// <param name="content">明文</param>
        /// <param name="key">密钥</param>
        /// <returns>加密后base64编码的密文</returns>
        public static string AesEncrypt(string content, string key)
        {
            try
            {
                //byte[] keyArray = Encoding.UTF8.GetBytes(Key);
                byte[] keyArray =Encoding.UTF8.GetBytes(key);
                byte[] toEncryptArray = Encoding.UTF8.GetBytes(content);

                RijndaelManaged rDel = new RijndaelManaged();
                rDel.Key = keyArray;
                rDel.Mode = CipherMode.ECB;
                rDel.Padding = PaddingMode.PKCS7;

                ICryptoTransform cTransform = rDel.CreateEncryptor();
                byte[] resultArray = cTransform.TransformFinalBlock(toEncryptArray, 0, toEncryptArray.Length);

                return Convert.ToBase64String(resultArray, 0, resultArray.Length);
            }
            catch (Exception ex)
            {
                return null;
            }
        }

        /// <summary>
        /// AES 算法解密
        /// </summary>
        /// <param name="content">密文</param>
        /// <param name="key">密钥</param>
        /// <returns>明文</returns>
        public static string AesDecrypt(string content, string key)
        {
            try
            {
                //byte[] keyArray = Encoding.UTF8.GetBytes(Key);
                byte[] keyArray = Encoding.UTF8.GetBytes(key);
                byte[] toEncryptArray = Convert.FromBase64String(content);

                RijndaelManaged rDel = new RijndaelManaged();
                rDel.Key = keyArray;
                rDel.Mode = CipherMode.ECB;
                rDel.Padding = PaddingMode.PKCS7;

                ICryptoTransform cTransform = rDel.CreateDecryptor();
                byte[] resultArray = cTransform.TransformFinalBlock(toEncryptArray, 0, toEncryptArray.Length);

                return Encoding.UTF8.GetString(resultArray);//  UTF8Encoding.UTF8.GetString(resultArray);
            }
            catch (Exception ex)
            {
                return null;
            }
        }

        public static string UrlEncode(string str)
        {
            if (str == null || str == "")
            {
                return null;
            }

            byte[] bytesToEncode = Encoding.UTF8.GetBytes(str);
            String returnVal = Convert.ToBase64String(bytesToEncode);

            return returnVal.TrimEnd('=').Replace('+', '-').Replace('/', '_');
        }

        public static string UrlDecode(string str)
        {
            if (str == null || str == "")
            {
                return null;
            }

            str= str.Replace('-', '+').Replace('_', '/');

            int paddings = str.Length % 4;
            if (paddings > 0)
            {
                str += new string('=', 4 - paddings);
            }

            byte[] encodedDataAsBytes = Convert.FromBase64String(str);
            string returnVal = Encoding.UTF8.GetString(encodedDataAsBytes);
            return returnVal;
        }
    }
}