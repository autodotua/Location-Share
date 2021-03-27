const keyUser = "user"

export class Location {
    public id: number = -1;
    public username: string = "";
    public latitude?: number;
    public longitude?: number;
    public altitude?: number;
    public accuracy?: number;
    public time: string="";
    public speed?: number;
}

export class User {
    public name: string = ""
    public password: string = ""
    public displayName?: string
    public groupName?: string 
    public token?: string
    public lastUpdateTime?: Date
    public lastLocation?: Location

    private static current: User | null
    public static getCurrent(): User | null {
        if (this.current == null) {

            if (localStorage.getItem(keyUser)===null) {
                return null;
            }
            this.current = JSON.parse(localStorage.getItem(keyUser)!)
        }
        return this.current;
    }
    public static setCurrent(user: User) {
        localStorage.setItem(keyUser, JSON.stringify(user))
        this.current = user;
    }

    public static applyChange()
    {
        localStorage.setItem(keyUser, JSON.stringify(this.current))

    }
}

export class Response<T>{
    public data?: T
    public message?: string
    public succeed: boolean = false
}