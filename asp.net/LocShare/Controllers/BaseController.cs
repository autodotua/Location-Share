using LocShare.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using LocShare.Service;

namespace LocShare.Controllers
{
    public abstract class BaseController : ControllerBase
    {
        private string username;

        public string UserName
        {
            get
            {
                if (username == null)
                {
                    username = TokenFilter.GetUserName(HttpContext);
                }
                return username;
            }
        }

        protected UserEntity GetUser(DbModel db)
        {
            return db.Find<UserEntity>(UserName);
        }
    }
}