package br.ufpe.cin.if1001.threads

import android.app.Activity
import android.os.Bundle
import android.net.Uri
import android.os.AsyncTask
import android.os.SystemClock
import android.support.v7.util.SortedList
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class RecyclerViewActivity : Activity() {
    internal var recyclerView: RecyclerView? = null
    private var task: AddPessoaTask? = null
    private var pessoaAdapter: PessoaAdapter? = null
    internal var listaPessoas: SortedList<Pessoa>? = null

    internal var metodosCallback: SortedList.Callback<Pessoa> = object : SortedList.Callback<Pessoa>() {
        override fun compare(o1: Pessoa, o2: Pessoa): Int {
            //return o1.getLogin().compareTo(o2.getLogin());
            return o1.nome!!.compareTo(o2.nome!!)
        }

        override fun onInserted(position: Int, count: Int) {
            pessoaAdapter?.notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            pessoaAdapter?.notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            pessoaAdapter?.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            pessoaAdapter?.notifyItemRangeChanged(position, count)
        }

        override fun areContentsTheSame(oldItem: Pessoa, newItem: Pessoa): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }

        override fun areItemsTheSame(item1: Pessoa, item2: Pessoa): Boolean {
            return compare(item1, item2) == 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        listaPessoas = SortedList(Pessoa::class.java, metodosCallback)
        pessoaAdapter = PessoaAdapter(listaPessoas)

        recyclerView = RecyclerView(this).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = pessoaAdapter
        }
        setContentView(recyclerView)
    }

    override fun onStart() {
        super.onStart()
        doAsync {
            uiThread {

            }
        }
        task = AddPessoaTask()
    }

    override fun onResume() {
        super.onResume()
        task?.execute(*Constants.maisPessoas)

    }

    override fun onPause() {
        super.onPause()
        task?.cancel(true)
    }

    private inner class PessoaAdapter(private val pessoas: SortedList<Pessoa>?) : RecyclerView.Adapter<CardChangeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardChangeHolder {
            val v = layoutInflater.inflate(R.layout.pessoa_cardview, parent, false)
            return CardChangeHolder(v)
        }

        override fun onBindViewHolder(holder: CardChangeHolder, position: Int) {
            holder.bindModel(pessoas?.get(position))
        }

        override fun getItemCount(): Int {
            if (pessoas!=null)
                return pessoas.size()
            else
                return 0
        }
    }

    internal class CardChangeHolder//poderia tambem passar algum objeto aqui construido no adapter, para nao adicionar atributos
    (row: View) : RecyclerView.ViewHolder(row), View.OnClickListener {
        var nome: TextView? = null
        var login: TextView? = null
        var icone: ImageView? = null
        var site: Uri? = null

        init {

            nome = row.findViewById(R.id.nome)
            login = row.findViewById(R.id.login)
            icone = row.findViewById(R.id.icone)

            icone!!.setOnClickListener(this)
        }

        fun bindModel(p: Pessoa?) {
            nome?.text = p?.nome
            login?.text = p?.login
            site = Uri.parse(p?.site)

            if (p?.login == "lmt") {
                icone?.setImageResource(R.drawable.ok)
            } else {
                icone?.setImageResource(R.drawable.delete)
            }
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            Toast.makeText(v.context, "Clicou no item da posição: $position", Toast.LENGTH_SHORT).show()

        }
    }

    private inner class AddPessoaTask : AsyncTask<Pessoa, Pessoa, Void>() {

        override fun doInBackground(vararg params: Pessoa): Void? {
            for (p in params) {
                if (isCancelled) {
                    break
                }
                publishProgress(p)
                SystemClock.sleep(1000)
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Pessoa) {
            if (!isCancelled) {
                listaPessoas?.add(values[0])
            }
        }

        override fun onPostExecute(aVoid: Void) {
            Toast.makeText(applicationContext, "Terminou!", Toast.LENGTH_SHORT).show()
            task = null
        }
    }
}