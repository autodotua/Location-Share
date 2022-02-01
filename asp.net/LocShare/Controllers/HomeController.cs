using LocShare.Service;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;
using System.Net;
using System.Threading.Tasks;

namespace LocShare.Controllers
{
    [Route("test")]
    public class HomeController : BaseController
    {
        [HttpGet]
        [Route("index")]
        public string Index()
        {
            return "服务正在运行";
        }
    }
}