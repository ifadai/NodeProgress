package com.fadai.sample

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.fadai.nodeprogress.NodeProgressBar
import com.fadai.sample.R.id.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start.setOnClickListener {
            if (et_node_count.text.isEmpty()) {
                showToast("请输入节点数")
            } else {
                try {
                    var str = et_node_count.text.toString()
                    var count = str.toInt()
                    if (count < 1) {
                        showToast("节点数不能小于1")
                    } else {
                        npb.setCount(count)
                        npb.start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("出错")
                }

            }
        }
        btn_reset.setOnClickListener {
            npb.reset()
        }

        npb.progressListener = object : NodeProgressBar.OnProgressListener {
            override fun onRequestScuccess(index: Int) {
                showToast("请求成功 $index")
            }

            override fun onRequestFailure(index: Int) {
                showToast("请求失败 $index")
            }

            override fun onComplete() {
                showToast("完成")
            }
        }

        btn_request_success1.setOnClickListener {
            npb.setRequestStatus(true, 0)
        }
        btn_request_success2.setOnClickListener {
            npb.setRequestStatus(true, 1)
        }
        btn_request_success3.setOnClickListener {
            npb.setRequestStatus(true, 2)
        }
        btn_request_success4.setOnClickListener {
            npb.setRequestStatus(true, 3)
        }
        btn_request_failure1.setOnClickListener {
            npb.setRequestStatus(false, 0)
        }
        btn_request_failure2.setOnClickListener {
            npb.setRequestStatus(false, 1)
        }
        btn_request_failure3.setOnClickListener {
            npb.setRequestStatus(false, 2)
        }
        btn_request_failure4.setOnClickListener {
            npb.setRequestStatus(false, 3)
        }
    }

    fun showToast(text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
    }


}
