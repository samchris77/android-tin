
_log-add 에서 전체 UI manual mode, auto mode 에서 달라짐._
_manual mode에서 swipe 방향 반대임_
_

when leaving the log progress tracking page the calender and week show is correctly returned to this date, but it doesn't work when going to the frequency or profile page and comming back.

also in log history, revert the view back to the very top when leaving the page and returning


- privacy 페이지로 넘어가는 링크 제대로 구현
_이외 'Your progress', 'about'는 맨 아래 남기고 나머지 component 들은 다 빼버리기_

_in 'Log' when switching between add, progress tracking, history make the animation like swipping the screen pass by, also when you swipe the screen make the page change acordingly._

다운 이후 처음으로 앱 열었을때 앱 구성에 대한 간단한 소개, 이명이 있으면 어떻게 사용하면 되는지  방법 설명, profile 페이지에서 설명 다시보기 가능하게  
- the very first screen should be totaly non transparent
- the second screen, show the 'frequency match' page in the back and make it more visable. put arrow like UI around the ball in the middle to indicate it is movable
- the third page, show the log-add page.  make a short animation of moving the tinnitus level from 7 to 4 and stress from 6 to 2, and then press the save entry

forth step slide the page to log-progress and give short explenation on that page

show mock data if there isn't any data saved

fifth step slide to the log-history page, and tell the user that the individual entrys are visiable here


show mock data if there isn't any data saved

next go to the profile page and tell the user you can setup alarms

_remove the export data button in log page_

_in log-progress traking, make the session calendar date numbers light grey, darker than the background but less prominant_

_in log-add after a entry is saved show short popup message to the user that session was save with info of the session_

_when loging a session with 0 minutes, make sure in log-history the entry is shown as 0 min, not No session_

_also make the Created time come next to  the ' # n ' on the same row and make the loudness and stress icon bigger to fit the hight, make each entry card hight shorter_
 

_log-progress 페이지에서 다른 페이지로 갔다가 다시 돌아오면 calender와 week 상태를 현재 시점에 맞게 다시 돌려놓기_


_소리 지속 타이머 설정 기능 및 및 특정 시간대 들으라고 알림 보내는 기능_

frequency 페이지에서 토글을 설정해서 Pink, brown noise 추가

_white noise 낮은 주파수에서 음량이 들리지 않는 문제 해결_

_log-progress tracking에서 GitHub code commit 달력처럼 들었는 시간에 따라서 달력 칸 네모 색상이 바뀌면 얼마나 열심히 꾸준히 들었는지 볼 수 있을거 같음_



https://tinweb-3544e.web.app/privacy

web 정보 수정

https://tinweb-3544e.web.app


