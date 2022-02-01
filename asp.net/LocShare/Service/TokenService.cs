using FzLib;
using FzLib.Cryptography;
using LocShare.Controllers;
using LocShare.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Primitives;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;

namespace LocShare.Service
{
    public class TokenFilter : ActionFilterAttribute
    {
        public static readonly string TokenPassword = "helloworld";

        /// <summary>
        /// Token有效期
        /// </summary>
        public static readonly TimeSpan TokenPeriod = TimeSpan.FromDays(365);

        private static RijndaelManaged aes;

        private readonly IConfiguration config;

        static TokenFilter()
        {
            aes = AesExtension.CreateManager();
            aes.SetStringIV("");
            aes.SetStringKey(TokenPassword);
        }

        //MThJOTVLYm9TMXZzVmRwcjVzVnBiNWhRTWE0bjQ5M3NqeGJoVnkrc1ZvRT0
        public TokenFilter(IConfiguration config)
        {
            this.config = config;
        }

        public static string GetToken(UserEntity user)
        {
            string name = user.Name;
            string timeString = DateTime.UtcNow.ToBinary().ToString().PadLeft(20);
            return UrlEncode(aes.Encrypt(timeString + name));
        }

        public static string GetUserName(HttpContext http)
        {
            if (!http.Request.Headers.ContainsKey("Authorization")
            || StringValues.IsNullOrEmpty(http.Request.Headers["Authorization"])
            || http.Request.Headers["Authorization"].FirstOrDefault() == "undefined")
            {
                throw new Exception("需要Token");
            }
            string token = http.Request.Headers["Authorization"];
            if (token.StartsWith("Bearer"))
            {
                token = token.Split(' ')[1];
            }
            string decryption = aes.Decrypt(UrlDecode(token));
            return decryption[20..];
        }

        public bool IsTokenValid(HttpContext context, string token, out string message)
        {
            try
            {
                if (string.IsNullOrEmpty(token))
                {
                    message = "验证信息为空";
                    return false;
                }
                string decryption = aes.Decrypt(UrlDecode(token));
                long timeBin = long.Parse(decryption.Substring(0, 20));
                if (DateTime.FromBinary(timeBin).Add(TokenPeriod) < DateTime.UtcNow)
                {
                    message = "登录信息已过期";
                    return false;
                }
                message = "";
                return true;
            }
            catch (Exception ex)
            {
                message = "验证出错：" + ex.Message;
                return false;
            }
        }

        public override void OnActionExecuting(ActionExecutingContext context)
        {
            if (context.Controller is UserController)
            {
                return;
            }
            var http = context.HttpContext;

            if (!http.Request.Headers.ContainsKey("Authorization")
                || StringValues.IsNullOrEmpty(http.Request.Headers["Authorization"])
                || http.Request.Headers["Authorization"].FirstOrDefault() == "undefined")
            {
                context.Result = new UnauthorizedObjectResult("需要Token");
                return;
            }
            string token = http.Request.Headers["Authorization"];
            if (token.StartsWith("Bearer"))
            {
                token = token.Split(' ')[1];
            }
            if (!IsTokenValid(http, token, out string msg))
            {
                context.Result = new UnauthorizedObjectResult(msg);
                return;
            }
        }

        public static string UrlDecode(string str)
        {
            if (str == null || str == "")
            {
                return null;
            }

            str = str.Replace('-', '+').Replace('_', '/');

            int paddings = str.Length % 4;
            if (paddings > 0)
            {
                str += new string('=', 4 - paddings);
            }

            byte[] encodedDataAsBytes = Convert.FromBase64String(str);
            string returnVal = Encoding.UTF8.GetString(encodedDataAsBytes);
            return returnVal;
        }

        public static string UrlEncode(string str)
        {
            if (str == null || str == "")
            {
                return null;
            }

            byte[] bytesToEncode = Encoding.UTF8.GetBytes(str);
            string returnVal = Convert.ToBase64String(bytesToEncode);

            return returnVal.TrimEnd('=').Replace('+', '-').Replace('/', '_');
        }
    }
}